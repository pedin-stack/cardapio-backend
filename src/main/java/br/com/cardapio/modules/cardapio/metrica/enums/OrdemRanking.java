package br.com.cardapio.modules.cardapio.metrica.enums;

/**
 * Ordem do ranking. Empates (e {@code NOME}) seguem nome e id, para a paginação não repetir nem pular itens.
 */
public enum OrdemRanking {
    /** Maior valor primeiro. */
    MAIOR,
    /** Menor valor primeiro. */
    MENOR,
    /** Alfabética. */
    NOME
}
