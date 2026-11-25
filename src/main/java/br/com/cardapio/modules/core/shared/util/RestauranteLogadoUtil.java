package br.com.cardapio.modules.core.shared.util;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;

import java.util.UUID;

/**
 * Isolamento por restaurante (IDOR, blueprint §6.9): todo dado do gestor é filtrado pelo restaurante do JWT,
 * nunca por um id vindo do cliente.
 */
public final class RestauranteLogadoUtil {

    private RestauranteLogadoUtil() {
    }

    /**
     * @throws BusinessException {@code UNAUTHORIZED} (403) se o usuário não administra um restaurante
     */
    public static UUID findRestauranteIdOrThrow(final UsuarioUserDetailsDTO usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.restauranteId() == null) {
            throw new BusinessException(BusinessExceptionMessage.UNAUTHORIZED.getMessage());
        }
        return usuarioLogado.restauranteId();
    }
}
