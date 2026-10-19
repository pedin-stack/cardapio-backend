package br.com.cardapio.modules.cardapio.metrica.enums;

/**
 * Ranking de itens das métricas.
 */
public enum TipoRanking {
    /** Curtidas que ficaram ("pedidos"). */
    CURTIDOS,
    /** Curtidas desfeitas. */
    QUASE_PEDIDOS,
    /** Tempo médio de tela por visualização (todos os itens, inclusive os sem visualização). */
    TEMPO
}
