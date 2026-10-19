package br.com.cardapio.modules.cardapio.adicional.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Cadastro/atualização de adicional. Limites espelhados no formulário de adicionais do front.
 */
@Schema(description = "Dados de um adicional pago")
public record AdicionalRequestDTO(

        @Schema(description = "Nome do adicional", example = "Bacon extra")
        @JsonProperty("nome")
        @NotBlank(message = "O nome do adicional é obrigatório.")
        @Size(max = 60, message = "O nome do adicional não pode exceder 60 caracteres.")
        String nome,

        @Schema(description = "Valor em reais", example = "5.00")
        @JsonProperty("valor")
        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais.")
        BigDecimal valor,

        @Schema(description = "Itens que oferecem o adicional")
        @JsonProperty("itemIds")
        @Size(max = 1000, message = "No máximo 1000 itens.")
        List<UUID> itemIds
) {}
