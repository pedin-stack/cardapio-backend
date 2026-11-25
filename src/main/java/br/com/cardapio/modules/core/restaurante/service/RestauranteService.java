package br.com.cardapio.modules.core.restaurante.service;

import br.com.cardapio.config.cache.RedisCacheConfig;
import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.core.video.dto.response.VideoResponseDTO;
import br.com.cardapio.modules.core.video.enums.StatusVideo;
import br.com.cardapio.modules.core.video.event.VideoItemDescartadoEvent;
import br.com.cardapio.modules.core.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.core.midia.service.IMidiaService;
import br.com.cardapio.modules.core.perfil.model.Perfil;
import br.com.cardapio.modules.core.perfil.repository.IPerfilRepository;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteAparenciaRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteAtualizarRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteStatusRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.response.RestauranteResponseDTO;
import br.com.cardapio.modules.core.restaurante.event.RestauranteExcluidoEvent;
import br.com.cardapio.modules.core.restaurante.event.VideoRestauranteEnviadoEvent;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import br.com.cardapio.modules.core.usuario.model.Usuario;
import br.com.cardapio.modules.core.usuario.repository.IUsuarioRepository;
import br.com.cardapio.infrastructure.util.CaminhoArquivoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * CRUD de restaurantes. Toda mutação limpa o cache {@code userDetails}: o status do restaurante
 * decide se o gestor continua autenticado.
 */
@Service
@RequiredArgsConstructor
public class RestauranteService implements IRestauranteService {

    /** Slugs que colidem com rotas do front. */
    private static final Set<String> SLUGS_RESERVADOS = Set.of("login", "painel", "plataforma", "conheca", "api", "404", "500", "_next");

    private final IRestauranteRepository restauranteRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IPerfilRepository perfilRepository;
    private final IArmazenamentoArquivoService armazenamentoArquivoService;
    private final IArmazenamentoVideoService armazenamentoVideoService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<RestauranteResponseDTO> findAllRestaurantes(final String nome, final Pageable pageable) {
        final Page<Restaurante> pagina = restauranteRepository.findByNomeContainingIgnoreCase(nome == null ? "" : nome.trim(), pageable);
        // Logos e capa da página inteira numa só assinatura.
        final Map<String, String> urls = armazenamentoArquivoService.findUrlsAssinadas(pagina.getContent().stream()
                .flatMap(restaurante -> Stream.of(restaurante.getLogoPath(), restaurante.getCapaPath(), restaurante.getLogoCabecalhoPath()))
                .toList());
        return pagina.map(restaurante -> mapToRestauranteResponseDTO(restaurante, null, urls));
    }

    @Override
    @Transactional(readOnly = true)
    public RestauranteResponseDTO findRestauranteById(final UUID id) {
        final Restaurante restaurante = findRestauranteByIdOrThrow(id);
        return mapToRestauranteResponseDTO(restaurante, findGestor(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_USER_DETAILS, allEntries = true)
    public RestauranteResponseDTO saveRestaurante(final RestauranteRequestDTO dto) {
        validateSlugOrThrow(dto.slug(), null);
        validateImagensOrThrow(dto.logoPath(), dto.capaPath());
        if (usuarioRepository.existsByEmailIgnoreCase(dto.gestorEmail().trim())) {
            throw new BusinessException(BusinessExceptionMessage.EMAIL_ALREADY_EXISTS.getMessage());
        }

        final Restaurante restaurante = new Restaurante();
        restaurante.setNome(dto.nome().trim());
        restaurante.setSlug(dto.slug());
        restaurante.setDescricao(dto.descricao());
        restaurante.setLogoPath(dto.logoPath());
        restaurante.setCapaPath(dto.capaPath());
        restaurante.setCorDestaque(normalizarCor(dto.corDestaque()));
        restaurante.setAtivo(true);
        final Restaurante salvo = restauranteRepository.save(restaurante);

        final Perfil perfilGestor = perfilRepository.findByNome(Perfil.GESTOR_RESTAURANTE)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage()));
        final Usuario gestor = new Usuario();
        gestor.setNome(dto.gestorNome().trim());
        gestor.setEmail(dto.gestorEmail().trim().toLowerCase());
        gestor.setSenha(passwordEncoder.encode(dto.gestorSenha()));
        gestor.setPerfil(perfilGestor);
        gestor.setRestaurante(salvo);
        usuarioRepository.save(gestor);

        return mapToRestauranteResponseDTO(salvo, gestor);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_USER_DETAILS, allEntries = true)
    public RestauranteResponseDTO updateRestaurante(final UUID id, final RestauranteAtualizarRequestDTO dto) {
        final Restaurante restaurante = findRestauranteByIdOrThrow(id);
        validateSlugOrThrow(dto.slug(), id);
        validateImagensOrThrow(dto.logoPath(), dto.capaPath());

        restaurante.setNome(dto.nome().trim());
        restaurante.setSlug(dto.slug());
        restaurante.setDescricao(dto.descricao());
        restaurante.setLogoPath(dto.logoPath());
        restaurante.setCapaPath(dto.capaPath());
        restaurante.setCorDestaque(normalizarCor(dto.corDestaque()));

        final Usuario gestor = findGestor(id);
        if (gestor != null && dto.gestorSenha() != null && !dto.gestorSenha().isBlank()) {
            gestor.setSenha(passwordEncoder.encode(dto.gestorSenha()));
        }
        return mapToRestauranteResponseDTO(restaurante, gestor);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_USER_DETAILS, allEntries = true)
    public RestauranteResponseDTO updateRestauranteStatus(final UUID id, final RestauranteStatusRequestDTO dto) {
        final Restaurante restaurante = findRestauranteByIdOrThrow(id);
        restaurante.setAtivo(Boolean.TRUE.equals(dto.ativo()));
        return mapToRestauranteResponseDTO(restaurante, null);
    }

    @Override
    @Transactional(readOnly = true)
    public RestauranteResponseDTO findRestauranteLogado(final UsuarioUserDetailsDTO usuarioLogado) {
        return mapToRestauranteResponseDTO(findRestauranteByIdOrThrow(RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado)), null);
    }

    @Override
    @Transactional
    public RestauranteResponseDTO updateAparenciaRestauranteLogado(final UsuarioUserDetailsDTO usuarioLogado,
                                                                    final RestauranteAparenciaRequestDTO dto) {
        // O restaurante vem da sessão, nunca do corpo: o gestor só muda o próprio.
        final Restaurante restaurante = findRestauranteByIdOrThrow(RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado));
        final String pasta = IMidiaService.findPastaRestaurante(restaurante.getId());
        final String logoCabecalho = vazioParaNulo(dto.logoCabecalhoPath());
        final String video = vazioParaNulo(dto.videoCaminho());
        // Logo e vídeo só podem vir dos uploads do próprio gestor.
        for (final String caminho : new String[]{logoCabecalho, video}) {
            if (caminho != null) {
                CaminhoArquivoUtil.validateCaminhoOrThrow(caminho, pasta);
            }
        }

        restaurante.setCorDestaque(normalizarCor(dto.corDestaque()));
        restaurante.setLogoCabecalhoPath(logoCabecalho);
        restaurante.setWhatsapp(vazioParaNulo(dto.whatsapp()));
        restaurante.setInstagram(vazioParaNulo(dto.instagram()));
        updateVideo(restaurante, video);
        return mapToRestauranteResponseDTO(restaurante, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_USER_DETAILS, allEntries = true)
    public void deleteRestauranteById(final UUID id) {
        restauranteRepository.delete(findRestauranteByIdOrThrow(id));
        eventPublisher.publishEvent(new RestauranteExcluidoEvent(id));
    }

    // ---- Helpers Privados ----

    private Restaurante findRestauranteByIdOrThrow(final UUID id) {
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage()));
    }

    private Usuario findGestor(final UUID restauranteId) {
        return usuarioRepository.findByRestauranteId(restauranteId).stream().findFirst().orElse(null);
    }

    private void validateSlugOrThrow(final String slug, final UUID idAtual) {
        if (SLUGS_RESERVADOS.contains(slug)) {
            throw new BusinessException("Este endereço é reservado pelo sistema. Escolha outro.");
        }
        final boolean emUso = idAtual == null
                ? restauranteRepository.existsBySlug(slug)
                : restauranteRepository.existsBySlugAndIdNot(slug, idAtual);
        if (emUso) {
            throw new BusinessException(BusinessExceptionMessage.SLUG_ALREADY_EXISTS.getMessage());
        }
    }

    /** Logo e capa só podem vir da pasta da plataforma (uploads assinados do super admin). */
    private void validateImagensOrThrow(final String logoPath, final String capaPath) {
        for (final String caminho : new String[]{logoPath, capaPath}) {
            if (caminho != null && !caminho.isBlank()) {
                CaminhoArquivoUtil.validateCaminhoOrThrow(caminho, IMidiaService.PASTA_PLATAFORMA);
            }
        }
    }

    /** Vazio = cor padrão; hex sempre minúsculo (o formato já foi validado no DTO). */
    private static String normalizarCor(final String cor) {
        return cor == null || cor.isBlank() ? null : cor.toLowerCase();
    }

    private static String vazioParaNulo(final String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    /**
     * Vídeo da capa, com a mesma regra do vídeo do item: novo → PROCESSANDO + evento (transcodifica após o commit);
     * removido → SEM_VIDEO; o mesmo de antes → nada muda (salvar a aparência não reprocessa).
     */
    private void updateVideo(final Restaurante restaurante, final String novo) {
        if (Objects.equals(novo, restaurante.getVideoOriginalPath())) {
            return;
        }
        if (restaurante.getVideoHlsPath() != null) {
            eventPublisher.publishEvent(new VideoItemDescartadoEvent(restaurante.getVideoHlsPath()));
        }
        restaurante.setVideoOriginalPath(novo);
        restaurante.setVideoHlsPath(null);
        restaurante.setVideoPosterPath(null);
        restaurante.setVideoStatus(novo == null ? StatusVideo.SEM_VIDEO : StatusVideo.PROCESSANDO);
        if (novo != null) {
            eventPublisher.publishEvent(new VideoRestauranteEnviadoEvent(restaurante.getId(), novo));
        }
    }

    // ---- Mappers Privados ----

    private RestauranteResponseDTO mapToRestauranteResponseDTO(final Restaurante restaurante, final Usuario gestor) {
        final List<String> caminhos = new ArrayList<>();
        caminhos.add(restaurante.getLogoPath());
        caminhos.add(restaurante.getCapaPath());
        caminhos.add(restaurante.getLogoCabecalhoPath());
        return mapToRestauranteResponseDTO(restaurante, gestor, armazenamentoArquivoService.findUrlsAssinadas(caminhos));
    }

    /** {@code urls}: caminho → URL assinada (bucket privado), já resolvidas em lote. */
    private RestauranteResponseDTO mapToRestauranteResponseDTO(final Restaurante restaurante, final Usuario gestor,
                                                               final Map<String, String> urls) {
        return new RestauranteResponseDTO(
                restaurante.getId(),
                restaurante.getNome(),
                restaurante.getSlug(),
                restaurante.getDescricao(),
                restaurante.getLogoPath(),
                restaurante.getLogoPath() != null ? urls.get(restaurante.getLogoPath()) : null,
                restaurante.getCapaPath(),
                restaurante.getCapaPath() != null ? urls.get(restaurante.getCapaPath()) : null,
                restaurante.getCorDestaque(),
                restaurante.getLogoCabecalhoPath(),
                restaurante.getLogoCabecalhoPath() != null ? urls.get(restaurante.getLogoCabecalhoPath()) : null,
                new VideoResponseDTO(
                        restaurante.getVideoStatus(),
                        restaurante.getVideoOriginalPath(),
                        armazenamentoVideoService.findUrlPublica(restaurante.getVideoHlsPath()),
                        armazenamentoVideoService.findUrlPublica(restaurante.getVideoPosterPath())),
                restaurante.getWhatsapp(),
                restaurante.getInstagram(),
                restaurante.isAtivo(),
                restaurante.getCriadoEm(),
                gestor != null ? gestor.getNome() : null,
                gestor != null ? gestor.getEmail() : null);
    }
}
