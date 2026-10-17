package br.com.cardapio.infrastructure.armazenamento;

import br.com.cardapio.infrastructure.exception.BusinessException;

import java.nio.file.Path;

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

    /** URL pública do objeto (bucket público), ou {@code null} se o caminho for nulo. */
    String findUrlPublica(String caminho);
}
