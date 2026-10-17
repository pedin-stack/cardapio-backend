package br.com.cardapio.infrastructure.armazenamento;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Storage do Supabase pela API REST, autenticado com a service-role key.
 * <p>
 * Desvio do blueprint §9.3 (S3): a API REST é a única que emite URL de upload assinada, então uma só
 * credencial cobre upload assinado, download do original e gravação do HLS (ADR-0001).
 * </p>
 * <p>
 * Bucket privado: leitura só por URL assinada. Assinamos por {@value #VALIDADE_HORAS} h e guardamos no Redis por
 * metade disso — toda URL entregue vale ≥ 12 h, e abrir o cardápio não vira uma chamada ao Supabase por foto.
 * </p>
 */
@Slf4j
@Service
public class SupabaseArmazenamentoService implements IArmazenamentoArquivoService {

    private static final Pattern CAMINHO_PERMITIDO = Pattern.compile("[A-Za-z0-9._/-]{1,400}");
    private static final long VALIDADE_HORAS = 24;
    private static final Duration CACHE_URL = Duration.ofHours(VALIDADE_HORAS / 2);
    private static final String PREFIXO_CACHE = "url_assinada:";

    private final RestClient restClient;
    private final StringRedisTemplate redisTemplate;
    private final String supabaseUrl;
    private final String bucket;

    public SupabaseArmazenamentoService(@Value("${app.supabase.url}") final String supabaseUrl,
                                        @Value("${app.supabase.service-key}") final String serviceKey,
                                        @Value("${app.supabase.bucket}") final String bucket,
                                        final StringRedisTemplate redisTemplate) {
        this.supabaseUrl = supabaseUrl.replaceAll("/+$", "");
        this.bucket = bucket;
        this.redisTemplate = redisTemplate;
        this.restClient = RestClient.builder()
                .baseUrl(this.supabaseUrl + "/storage/v1")
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .defaultHeader("apikey", serviceKey)
                .build();
    }

    @Override
    public String createUploadToken(final String caminho) {
        validateCaminhoOrThrow(caminho);
        try {
            final Map<?, ?> resposta = restClient.post()
                    .uri(objectPath("/object/upload/sign/", caminho))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .retrieve()
                    .body(Map.class);
            // Resposta: {"url": "/object/upload/sign/<bucket>/<caminho>?token=<jwt>"}
            final String url = resposta != null ? String.valueOf(resposta.get("url")) : "";
            final String token = UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("token");
            if (token == null) {
                throw new BusinessException(BusinessExceptionMessage.STORAGE_FAILED.getMessage());
            }
            return token;
        } catch (final RestClientException ex) {
            throw new BusinessException(BusinessExceptionMessage.STORAGE_FAILED.getMessage(), ex);
        }
    }

    @Override
    public void downloadArquivo(final String caminho, final Path destino) {
        validateCaminhoOrThrow(caminho);
        try {
            restClient.get()
                    .uri(objectPath("/object/", caminho))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isError()) {
                            throw new BusinessException(BusinessExceptionMessage.STORAGE_FAILED.getMessage());
                        }
                        Files.copy(response.getBody(), destino, StandardCopyOption.REPLACE_EXISTING);
                        return null;
                    });
        } catch (final RestClientException | UncheckedIOException ex) {
            throw new BusinessException(BusinessExceptionMessage.STORAGE_FAILED.getMessage(), ex);
        }
    }

    @Override
    public void saveArquivo(final String caminho, final Path origem, final String tipoConteudo) {
        validateCaminhoOrThrow(caminho);
        try {
            restClient.post()
                    .uri(objectPath("/object/", caminho))
                    .header("x-upsert", "true")
                    // Segmentos HLS são imutáveis (nome único por vídeo): cache longo no CDN/navegador.
                    .header("cache-control", "max-age=31536000")
                    .contentType(MediaType.parseMediaType(tipoConteudo))
                    .contentLength(Files.size(origem))
                    .body(new FileSystemResource(origem))
                    .retrieve()
                    .toBodilessEntity();
        } catch (final RestClientException | IOException ex) {
            throw new BusinessException(BusinessExceptionMessage.STORAGE_FAILED.getMessage(), ex);
        }
    }

    @Override
    public void deleteArquivo(final String caminho) {
        try {
            validateCaminhoOrThrow(caminho);
            restClient.delete().uri(objectPath("/object/", caminho)).retrieve().toBodilessEntity();
        } catch (final RuntimeException ex) {
            log.warn("Não foi possível apagar {} do storage: {}", caminho, ex.getMessage());
        }
    }

    /** Caminho já validado (sem chaves nem segmentos especiais): concatenar preserva as barras, que um path variable codificaria como %2F. */
    private String objectPath(final String base, final String caminho) {
        return base + bucket + "/" + caminho;
    }

    @Override
    public Map<String, String> findUrlsAssinadas(final Collection<String> caminhos) {
        final List<String> distintos = caminhos.stream().filter(Objects::nonNull).distinct().toList();
        if (distintos.isEmpty()) {
            return Map.of();
        }
        distintos.forEach(SupabaseArmazenamentoService::validateCaminhoOrThrow);

        final Map<String, String> urls = new HashMap<>(lerCache(distintos));
        final List<String> faltando = distintos.stream().filter(caminho -> !urls.containsKey(caminho)).toList();
        if (!faltando.isEmpty()) {
            final Map<String, String> novas = assinar(faltando);
            urls.putAll(novas);
            gravarCache(novas);
        }
        return urls;
    }

    /** POST /object/sign/{bucket} com {expiresIn, paths}: uma chamada para o lote inteiro. */
    private Map<String, String> assinar(final List<String> caminhos) {
        try {
            final List<Map<String, Object>> resposta = restClient.post()
                    .uri("/object/sign/" + bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", Duration.ofHours(VALIDADE_HORAS).toSeconds(), "paths", caminhos))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            final Map<String, String> urls = new HashMap<>();
            if (resposta != null) {
                for (final Map<String, Object> item : resposta) {
                    // signedURL vem relativo a /storage/v1 (ex.: /object/sign/<bucket>/<caminho>?token=...).
                    if (item.get("path") instanceof String caminho && item.get("signedURL") instanceof String relativa) {
                        urls.put(caminho, supabaseUrl + "/storage/v1" + relativa);
                    }
                }
            }
            return urls;
        } catch (final RestClientException ex) {
            log.warn("Não foi possível assinar {} URL(s) do storage: {}", caminhos.size(), ex.getMessage());
            return Map.of();
        }
    }

    /** Cache é otimização: Redis fora do ar só faz assinar de novo. */
    private Map<String, String> lerCache(final List<String> caminhos) {
        try {
            final List<String> valores = redisTemplate.opsForValue()
                    .multiGet(caminhos.stream().map(caminho -> PREFIXO_CACHE + caminho).toList());
            final Map<String, String> urls = new HashMap<>();
            for (int i = 0; valores != null && i < caminhos.size(); i++) {
                if (valores.get(i) != null) {
                    urls.put(caminhos.get(i), valores.get(i));
                }
            }
            return urls;
        } catch (final RuntimeException ex) {
            log.warn("Cache de URLs assinadas indisponível: {}", ex.getMessage());
            return Map.of();
        }
    }

    private void gravarCache(final Map<String, String> urls) {
        try {
            urls.forEach((caminho, url) -> redisTemplate.opsForValue().set(PREFIXO_CACHE + caminho, url, CACHE_URL));
        } catch (final RuntimeException ex) {
            log.warn("Cache de URLs assinadas indisponível: {}", ex.getMessage());
        }
    }

    /**
     * Recusa caminho com segmento vazio, "." ou ".." (o caminho pode vir do cliente ou de dado persistido).
     */
    public static void validateCaminhoOrThrow(final String caminho) {
        // Whitelist: o caminho � concatenado na URL do storage (sem chaves de template, '?', '#', '%' ou '\').
        if (caminho == null || !CAMINHO_PERMITIDO.matcher(caminho).matches()) {
            throw new BusinessException(BusinessExceptionMessage.CAMINHO_ARQUIVO_INVALIDO.getMessage());
        }
        for (final String segmento : caminho.split("/", -1)) {
            if (segmento.isEmpty() || ".".equals(segmento) || "..".equals(segmento)) {
                throw new BusinessException(BusinessExceptionMessage.CAMINHO_ARQUIVO_INVALIDO.getMessage());
            }
        }
    }

    /** Valida o caminho e exige que ele esteja dentro de {@code prefixo} (isolamento por restaurante). */
    public static void validateCaminhoOrThrow(final String caminho, final String prefixo) {
        validateCaminhoOrThrow(caminho);
        if (!caminho.startsWith(prefixo)) {
            throw new BusinessException(BusinessExceptionMessage.CAMINHO_ARQUIVO_INVALIDO.getMessage());
        }
    }
}
