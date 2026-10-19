package br.com.cardapio.modules.cardapio.avaliacao.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Estrelas dadas a um item pelo cliente identificado.
 */
@Schema(description = "Avaliação de um item")
public record AvaliacaoRequestDTO(

        @Schema(description = "Estrelas, de 1 a 5", example = "5")
        @JsonProperty("estrelas")
        @NotNull(message = "Escolha de 1 a 5 estrelas.")
        @Min(value = 1, message = "Escolha de 1 a 5 estrelas.")
        @Max(value = 5, message = "Escolha de 1 a 5 estrelas.")
        Integer estrelas
) {}
