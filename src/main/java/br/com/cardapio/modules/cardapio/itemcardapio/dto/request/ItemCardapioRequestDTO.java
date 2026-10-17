package br.com.cardapio.modules.cardapio.itemcardapio.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Cadastro/atualização de item (mesmo DTO: o formulário do painel envia o item inteiro).
 * Limites espelhados em {@code itemCardapio.schema.ts} do front.
 */
@Schema(description = "Dados de um item do cardápio")
public record ItemCardapioRequestDTO(

        @Schema(description = "Nome do prato", example = "Cappuccino italiano")
        @JsonProperty("nome")
        @NotBlank(message = "O nome do prato é obrigatório.")
        @Size(max = 120, message = "O nome do prato não pode exceder 120 caracteres.")
        String nome,

        @Schema(description = "Descrição do prato", example = "Espresso duplo, leite vaporizado e cacau.")
        @JsonProperty("descricao")
        @Size(max = 2000, message = "A descrição não pode exceder 2000 caracteres.")
        String descricao,

        @Schema(description = "Valor em reais", example = "14.90")
        @JsonProperty("valor")
        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais.")
        BigDecimal valor,

        @Schema(description = "Se aparece no cardápio público (padrão: true)", example = "true")
        @JsonProperty("ativo")
        Boolean ativo,

        @Schema(description = "Posição no feed (menor primeiro)", example = "0")
        @JsonProperty("ordem")
        @Min(value = 0, message = "A ordem não pode ser negativa.")
        @Max(value = 9999, message = "A ordem não pode exceder 9999.")
        Integer ordem,

        @Schema(description = "Categorias do item (do próprio restaurante)")
        @JsonProperty("categoriaIds")
        @Size(max = 20, message = "Um item pode ter no máximo 20 categorias.")
        List<@NotNull(message = "Categoria inválida.") UUID> categoriaIds,

        @Schema(description = "Ingredientes, na ordem de exibição", example = "[\"espresso\", \"leite\", \"cacau\"]")
        @JsonProperty("ingredientes")
        @Size(max = 40, message = "Um item pode ter no máximo 40 ingredientes.")
        List<@NotBlank(message = "Ingrediente vazio.") @Size(max = 80, message = "Cada ingrediente pode ter no máximo 80 caracteres.") String> ingredientes,

        @Schema(description = "Caminhos das imagens no storage, na ordem do carrossel (máx. 8)")
        @JsonProperty("imagens")
        @Size(max = 8, message = "Cada item pode ter no máximo 8 imagens.")
        List<@NotBlank(message = "Imagem inválida.") String> imagens,

        @Schema(description = "Caminho do vídeo original no storage; ausente remove o vídeo")
        @JsonProperty("videoCaminho")
        @Size(max = 500, message = "O caminho do vídeo não pode exceder 500 caracteres.")
        String videoCaminho
) {}
