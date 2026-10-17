package br.com.cardapio.modules.cardapio.publico.dto.response;

import br.com.cardapio.modules.cardapio.categoria.dto.response.CategoriaResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Cardápio público completo numa única resposta (o feed inteiro cabe em uma ida à rede).
 */
@Schema(description = "Cardápio público do restaurante")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CardapioPublicoResponseDTO(

        @Schema(description = "Restaurante")
        @JsonProperty("restaurante")
        RestaurantePublicoResponseDTO restaurante,

        @Schema(description = "Categorias (só as que têm itens ativos)")
        @JsonProperty("categorias")
        List<CategoriaResponseDTO> categorias,

        @Schema(description = "Itens ativos na ordem do feed")
        @JsonProperty("itens")
        List<ItemPublicoResponseDTO> itens
) {}
