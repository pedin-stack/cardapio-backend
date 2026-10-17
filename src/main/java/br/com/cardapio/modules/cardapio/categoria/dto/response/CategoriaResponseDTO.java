package br.com.cardapio.modules.cardapio.categoria.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Categoria do cardápio.
 */
@Schema(description = "Categoria do cardápio")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoriaResponseDTO(

        @Schema(description = "Identificador da categoria")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome", example = "Cafés")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Posição", example = "1")
        @JsonProperty("ordem")
        int ordem
) {}
