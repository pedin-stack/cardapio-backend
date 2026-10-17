package br.com.cardapio.modules.cardapio.metrica.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Painel de métricas do restaurante num período.
 */
@Schema(description = "Métricas do cardápio no período")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricasResponseDTO(

        @Schema(description = "Início do período (inclusive)")
        @JsonProperty("inicio")
        LocalDate inicio,

        @Schema(description = "Fim do período (inclusive)")
        @JsonProperty("fim")
        LocalDate fim,

        @Schema(description = "Totais do período")
        @JsonProperty("resumo")
        Resumo resumo,

        @Schema(description = "Tempo de tela por item (todos os itens, do mais visto ao menos)")
        @JsonProperty("tempoPorItem")
        List<TempoItem> tempoPorItem,

        @Schema(description = "Top 10 itens mais curtidos (curtidas que ficaram)")
        @JsonProperty("maisCurtidos")
        List<ContagemItem> maisCurtidos,

        @Schema(description = "Top 10 'quase pedidos' (curtidos e depois descurtidos)")
        @JsonProperty("quasePedidos")
        List<ContagemItem> quasePedidos,

        @Schema(description = "Curtidas por dia da semana (1 = segunda … 7 = domingo) e hora; só células com valor")
        @JsonProperty("curtidasPorHorario")
        List<ContagemHorario> curtidasPorHorario
) {

    /** Totais do período. */
    public record Resumo(
            @JsonProperty("visualizacoes") long visualizacoes,
            @JsonProperty("tempoTotalMs") long tempoTotalMs,
            @JsonProperty("curtidas") long curtidas,
            @JsonProperty("quasePedidos") long quasePedidos
    ) {}

    /** Tempo de tela de um item. */
    public record TempoItem(
            @JsonProperty("itemId") UUID itemId,
            @JsonProperty("nome") String nome,
            @JsonProperty("visualizacoes") long visualizacoes,
            @JsonProperty("tempoTotalMs") long tempoTotalMs,
            @JsonProperty("tempoMedioMs") long tempoMedioMs
    ) {}

    /** Contagem de curtidas de um item. */
    public record ContagemItem(
            @JsonProperty("itemId") UUID itemId,
            @JsonProperty("nome") String nome,
            @JsonProperty("total") long total
    ) {}

    /** Célula do mapa de calor. */
    public record ContagemHorario(
            @JsonProperty("diaSemana") int diaSemana,
            @JsonProperty("hora") int hora,
            @JsonProperty("total") long total
    ) {}
}
