package br.com.cardapio.modules.cardapio.categoria.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cadastro/atualização de categoria. Limites espelhados em {@code categoria.schema.ts} do front.
 */
@Schema(description = "Dados de uma categoria do cardápio")
public record CategoriaRequestDTO(

        @Schema(description = "Nome da categoria", example = "Cafés")
        @JsonProperty("nome")
        @NotBlank(message = "O nome da categoria é obrigatório.")
        @Size(max = 60, message = "O nome da categoria não pode exceder 60 caracteres.")
        String nome,

        @Schema(description = "Posição na barra de categorias (menor primeiro)", example = "1")
        @JsonProperty("ordem")
        @Min(value = 0, message = "A ordem não pode ser negativa.")
        @Max(value = 9999, message = "A ordem não pode exceder 9999.")
        Integer ordem
) {}
