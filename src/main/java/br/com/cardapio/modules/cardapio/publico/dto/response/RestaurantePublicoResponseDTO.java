package br.com.cardapio.modules.cardapio.publico.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Dados públicos do restaurante (sem gestor nem caminhos internos).
 */
@Schema(description = "Restaurante no cardápio público")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestaurantePublicoResponseDTO(

        @Schema(description = "Identificador")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome", example = "Café do Zé")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Slug", example = "cafe-do-ze")
        @JsonProperty("slug")
        String slug,

        @Schema(description = "Descrição")
        @JsonProperty("descricao")
        String descricao,

        @Schema(description = "URL do logo")
        @JsonProperty("logoUrl")
        String logoUrl,

        @Schema(description = "URL da capa (hero)")
        @JsonProperty("capaUrl")
        String capaUrl
) {}
