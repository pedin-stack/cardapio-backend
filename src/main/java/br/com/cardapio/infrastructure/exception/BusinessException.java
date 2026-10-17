package br.com.cardapio.infrastructure.exception;

/**
 * Exceção de regra de negócio. O status HTTP é resolvido pelo texto da mensagem em
 * {@link BusinessExceptionMessage#statusOf(String)}.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -6062815084191991722L;

    public BusinessException(final String message) {
        super(message);
    }

    public BusinessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
