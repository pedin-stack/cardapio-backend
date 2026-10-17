package br.com.cardapio.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Corpo padrão de erro da API; {@code stacktrace} e {@code errors} são omitidos quando vazios.
 */
@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String message;
    private String stacktrace;
    private List<ValidationError> errors;

    /** Erro de validação de um campo. */
    public record ValidationError(String field, String message) {
    }

    public void addValidationError(final String field, final String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }
}
