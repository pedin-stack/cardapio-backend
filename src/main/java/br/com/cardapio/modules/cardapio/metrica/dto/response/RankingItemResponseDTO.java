package br.com.cardapio.modules.cardapio.metrica.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Linha de um ranking de itens. Em {@code TEMPO}, {@code valor} é o tempo médio (ms) e vêm também as
 * visualizações e o tempo total; nos de curtidas, {@code valor} é a contagem.
 */
@Schema(description = "Item de um ranking das métricas")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RankingItemResponseDTO(

        @Schema(description = "Id do item")
        @JsonProperty("itemId")
        UUID itemId,

        @Schema(description = "Nome do item")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Curtidas, ou tempo médio em ms no ranking de tempo")
        @JsonProperty("valor")
        long valor,

        @Schema(description = "Visualizações no período (só no ranking de tempo)")
        @JsonProperty("visualizacoes")
        Long visualizacoes,

        @Schema(description = "Tempo total de tela em ms (só no ranking de tempo)")
        @JsonProperty("tempoTotalMs")
        Long tempoTotalMs
) {}
