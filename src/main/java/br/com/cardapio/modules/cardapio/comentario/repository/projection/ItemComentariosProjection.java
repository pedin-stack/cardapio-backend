package br.com.cardapio.modules.cardapio.comentario.repository.projection;

import java.util.UUID;

/**
 * Quantidade de comentários de um item.
 */
public interface ItemComentariosProjection {

    UUID getItemId();

    Long getTotal();
}
