package br.com.cardapio.modules.core.restaurante.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * Cor do cardápio escolhida pelo gestor no próprio painel. Ausente/nula volta à cor padrão.
 */
@Schema(description = "Cor de destaque do cardápio do restaurante logado")
public record RestauranteCorRequestDTO(

        @Schema(description = "Cor hex (ausente = padrão)", example = "#e9b872")
        @JsonProperty("corDestaque")
        @Pattern(regexp = RestauranteAtualizarRequestDTO.COR_REGEX, message = "Use uma cor no formato #rrggbb.")
        String corDestaque
) {}
