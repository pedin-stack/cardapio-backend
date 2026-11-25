package br.com.cardapio.infrastructure.util;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;

import java.util.regex.Pattern;

/**
 * Validação dos caminhos de arquivo no storage e no disco.
 */
public final class CaminhoArquivoUtil {

    private static final Pattern CAMINHO_PERMITIDO = Pattern.compile("[A-Za-z0-9._/-]{1,400}");

    private CaminhoArquivoUtil() {
    }

    /**
     * Recusa caminho com segmento vazio, "." ou ".." (o caminho pode vir do cliente ou de dado persistido).
     */
    public static void validateCaminhoOrThrow(final String caminho) {
        // Whitelist: o caminho é concatenado na URL do storage (sem chaves de template, '?', '#', '%' ou '\').
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
