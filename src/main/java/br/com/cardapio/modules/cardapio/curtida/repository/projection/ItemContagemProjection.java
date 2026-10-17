package br.com.cardapio.modules.cardapio.curtida.repository.projection;

import java.util.UUID;

/**
 * Contagem de curtidas por item.
 */
public interface ItemContagemProjection {

    UUID getItemId();

    String getNome();

    Long getTotal();
}
