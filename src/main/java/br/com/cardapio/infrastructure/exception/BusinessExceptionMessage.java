package br.com.cardapio.infrastructure.exception;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Catálogo de mensagens de negócio e do status HTTP associado a cada uma (blueprint §17.2.1).
 * <p>
 * Os services lançam {@code new BusinessException(BusinessExceptionMessage.X.getMessage())}; o
 * {@code GlobalExceptionHandler} devolve o status da constante. Mensagens não catalogadas resultam em 400.
 * </p>
 */
public enum BusinessExceptionMessage {

    // 404 ------------------------------------------------------------------
    NOT_FOUND("O registro solicitado não existe.", HttpStatus.NOT_FOUND),
    SERVICO_INDISPONIVEL("Serviço indisponível.", HttpStatus.NOT_FOUND),

    // 400 ------------------------------------------------------------------
    CAMINHO_ARQUIVO_INVALIDO("O caminho do arquivo é inválido.", HttpStatus.BAD_REQUEST),
    TIPO_ARQUIVO_NAO_SUPORTADO("Tipo de arquivo não suportado.", HttpStatus.BAD_REQUEST),
    CATEGORIA_INVALIDA("Uma ou mais categorias informadas não pertencem ao restaurante.", HttpStatus.BAD_REQUEST),
    ITEM_INVALIDO("Um ou mais itens informados não pertencem ao restaurante.", HttpStatus.BAD_REQUEST),

    // 401 ------------------------------------------------------------------
    INVALID_CREDENTIALS("E-mail ou senha inválidos.", HttpStatus.UNAUTHORIZED),
    CLIENTE_NAO_IDENTIFICADO("Identifique-se para continuar.", HttpStatus.UNAUTHORIZED),

    // 403 ------------------------------------------------------------------
    UNAUTHORIZED("Acesso negado.", HttpStatus.FORBIDDEN),
    USUARIO_INATIVO("Usuário inativo ou restaurante desativado.", HttpStatus.FORBIDDEN),

    // 409 ------------------------------------------------------------------
    EMAIL_ALREADY_EXISTS("Este e-mail já está sendo utilizado por um usuário no sistema.", HttpStatus.CONFLICT),
    SLUG_ALREADY_EXISTS("Este endereço (slug) já está sendo utilizado por outro restaurante.", HttpStatus.CONFLICT),
    CATEGORIA_ALREADY_EXISTS("Já existe uma categoria com este nome.", HttpStatus.CONFLICT),

    // 500 ------------------------------------------------------------------
    STORAGE_FAILED("Falha ao comunicar com o armazenamento de arquivos. Tente novamente mais tarde.", HttpStatus.INTERNAL_SERVER_ERROR);

    private static final Map<String, BusinessExceptionMessage> POR_MENSAGEM = Arrays.stream(values())
            .collect(Collectors.toMap(BusinessExceptionMessage::getMessage, Function.identity()));

    private final String message;
    private final HttpStatus status;

    BusinessExceptionMessage(final String message, final HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    /** Texto da mensagem (é o que vai no {@code BusinessException} e no corpo da resposta). */
    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /** Status HTTP da mensagem; 400 quando o texto não está catalogado. */
    public static HttpStatus statusOf(final String message) {
        final BusinessExceptionMessage encontrada = POR_MENSAGEM.get(message);
        return encontrada != null ? encontrada.status : HttpStatus.BAD_REQUEST;
    }
}
