package br.com.cardapio.modules.cardapio.avaliacao.repository.projection;

import java.util.UUID;

/**
 * Média das estrelas e quantidade de avaliações de um item.
 */
public interface ItemAvaliacaoProjection {

    UUID getItemId();

    Double getMedia();

    Long getTotal();
}
