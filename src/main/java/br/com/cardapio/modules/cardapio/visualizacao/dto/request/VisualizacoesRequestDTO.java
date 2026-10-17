package br.com.cardapio.modules.cardapio.visualizacao.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Lote de tempos de tela acumulados no navegador (enviado a cada 15 s e ao sair da página).
 */
@Schema(description = "Lote de visualizações")
public record VisualizacoesRequestDTO(

        @Schema(description = "Tempo por item desde o último envio")
        @JsonProperty("itens")
        @NotEmpty(message = "O lote de visualizações está vazio.")
        @Size(max = 100, message = "O lote pode ter no máximo 100 itens.")
        List<@NotNull(message = "Visualização inválida.") @Valid VisualizacaoItem> itens
) {

    /**
     * Tempo acumulado num item.
     */
    @Schema(description = "Tempo de tela de um item")
    public record VisualizacaoItem(

            @Schema(description = "Item visto")
            @JsonProperty("itemId")
            @NotNull(message = "O item é obrigatório.")
            UUID itemId,

            @Schema(description = "Milissegundos com o item na tela", example = "4200")
            @JsonProperty("duracaoMs")
            @NotNull(message = "A duração é obrigatória.")
            @Min(value = 0, message = "A duração não pode ser negativa.")
            Long duracaoMs
    ) {}
}
