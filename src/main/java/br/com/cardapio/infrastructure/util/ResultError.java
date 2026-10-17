package br.com.cardapio.infrastructure.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

/**
 * Corpo 422 do {@link BindingResult}: mapa campo → mensagem. Primeira instrução dos métodos POST/PUT.
 */
public final class ResultError {

    private ResultError() {
    }

    public static Map<String, String> getResultErrors(final BindingResult result) {
        final Map<String, String> erros = new HashMap<>();
        for (final FieldError erro : result.getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());
        }
        return erros;
    }
}
