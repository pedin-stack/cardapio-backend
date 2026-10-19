package br.com.cardapio.modules.cardapio.adicional.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Adicional no painel do gestor (com os itens ligados) e, sem {@code itemIds}, no cardápio público.
 */
@Schema(description = "Adicional pago")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdicionalResponseDTO(

        @Schema(description = "Identificador do adicional")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome", example = "Bacon extra")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Valor em reais", example = "5.00")
        @JsonProperty("valor")
        BigDecimal valor,

        @Schema(description = "Itens que oferecem o adicional (só no painel)")
        @JsonProperty("itemIds")
        List<UUID> itemIds
) {}
