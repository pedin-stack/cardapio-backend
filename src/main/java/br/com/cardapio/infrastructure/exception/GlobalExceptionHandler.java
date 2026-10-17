package br.com.cardapio.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Tratamento global de exceções: devolve {@link ErrorResponse} para toda exceção lançada numa requisição.
 */
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Expõe o 'stackTrace' ao cliente somente com server.error.include-exception=true. */
    @Value(value = "${server.error.include-exception:false}")
    private boolean printStackTrace;

    /** Validação de corpo (@Valid) não capturada via BindingResult no controller → 422. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException exception,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {

        final ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de validação. Veja o campo 'errors' para mais detalhes.");

        for (final FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    /** Valor inválido de path variable / request param → 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {
        final String errorMessage = String.format("Valor inválido para o parâmetro '%s'.", exception.getName());
        log.warn(errorMessage);
        return buildErrorMessage(exception, errorMessage, HttpStatus.BAD_REQUEST);
    }

    /** Violação de integridade (unicidade, FK) → 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(final DataIntegrityViolationException exception) {
        final String errorMessage = "Não é possível concluir a operação: o registro conflita com dados existentes.";
        log.warn(errorMessage, exception);
        return buildErrorMessage(exception, errorMessage, HttpStatus.CONFLICT);
    }

    /** Erro de negócio: o status vem do catálogo ({@link BusinessExceptionMessage#statusOf}). */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(final BusinessException exception) {
        final HttpStatus status = BusinessExceptionMessage.statusOf(exception.getMessage());
        if (status.is5xxServerError()) {
            log.error(exception.getMessage(), exception);
        } else {
            log.warn(exception.getMessage());
        }
        return buildErrorMessage(exception, exception.getMessage(), status);
    }

    /** Falta de autoridade (@PreAuthorize) → 403. */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(final RuntimeException exception) {
        log.warn("Acesso negado: {}", exception.getMessage());
        return buildErrorMessage(exception, "Acesso negado. Você não tem permissão para acessar esse recurso.", HttpStatus.FORBIDDEN);
    }

    /** Qualquer erro não tratado → 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(final Exception exception) {
        final String errorMessage = "Ocorreu um erro inesperado.";
        log.error(errorMessage, exception);
        return buildErrorMessage(exception, errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> buildErrorMessage(final Exception exception, final String message, final HttpStatus httpStatus) {
        final ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), message);
        if (printStackTrace) {
            errorResponse.setStacktrace(ExceptionUtils.getStackTrace(exception));
        }
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
