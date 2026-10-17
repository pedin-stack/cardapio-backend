package br.com.cardapio.modules.cardapio.curtida.repository.projection;

/**
 * Curtidas por dia da semana (ISO: 1 = segunda … 7 = domingo) e hora do dia (0–23).
 */
public interface HorarioContagemProjection {

    Integer getDiaSemana();

    Integer getHora();

    Long getTotal();
}
