package br.com.cardapio.infrastructure.armazenamento;

import br.com.cardapio.infrastructure.exception.BusinessException;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstração do storage de arquivos (o domínio não sabe que é o Supabase).
 * <p>
 * Uploads do navegador usam URL assinada emitida aqui; o back só lê originais e grava derivados (HLS).
 * </p>
 */
public interface IArmazenamentoArquivoService {

    /**
     * Emite um token de upload único para {@code caminho} (o front envia o arquivo direto ao bucket).
     *
     * @param caminho caminho do objeto dentro do bucket
     * @return token do upload assinado
     * @throws BusinessException {@code STORAGE_FAILED} se o storage recusar
     */
    String createUploadToken(String caminho);

    /**
     * Baixa o objeto para um arquivo local.
     *
     * @throws BusinessException {@code STORAGE_FAILED} se o download falhar
     */
    void downloadArquivo(String caminho, Path destino);

    /**
     * Grava (ou sobrescreve) um objeto a partir de um arquivo local.
     *
     * @throws BusinessException {@code STORAGE_FAILED} se o upload falhar
     */
    void saveArquivo(String caminho, Path origem, String tipoConteudo);

    /** Apaga o objeto (ex.: o vídeo original depois de virar HLS). Falha só gera log: nunca derruba o fluxo. */
    void deleteArquivo(String caminho);

    /**
     * URLs de leitura temporárias (o bucket é privado), assinadas em lote numa única chamada ao storage.
     * Toda URL devolvida vale por pelo menos 12 h. Caminhos nulos são ignorados; falha de assinatura deixa o
     * caminho fora do mapa (a imagem some da tela, a requisição não falha).
     *
     * @param caminhos caminhos dos objetos no bucket
     * @return caminho → URL assinada
     */
    Map<String, String> findUrlsAssinadas(Collection<String> caminhos);

    /** Atalho de {@link #findUrlsAssinadas} para um caminho; {@code null} se o caminho for nulo ou falhar. */
    default String findUrlAssinada(final String caminho) {
        return caminho == null ? null : findUrlsAssinadas(List.of(caminho)).get(caminho);
    }
}
