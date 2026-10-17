package br.com.cardapio.modules.cardapio.visualizacao.repository.projection;

import java.util.UUID;

/**
 * Tempo de tela por item no período.
 */
public interface ItemTempoProjection {

    UUID getItemId();

    String getNome();

    Long getVisualizacoes();

    Long getTempoTotalMs();

    Long getTempoMedioMs();
}
