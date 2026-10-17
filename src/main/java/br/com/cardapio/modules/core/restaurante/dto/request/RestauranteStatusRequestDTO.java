package br.com.cardapio.modules.core.restaurante.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Ativa ou desativa um restaurante.
 */
@Schema(description = "Novo status do restaurante")
public record RestauranteStatusRequestDTO(

        @Schema(description = "true libera o cardápio; false responde 'Serviço indisponível'", example = "false")
        @JsonProperty("ativo")
        @NotNull(message = "O status é obrigatório.")
        Boolean ativo
) {}
