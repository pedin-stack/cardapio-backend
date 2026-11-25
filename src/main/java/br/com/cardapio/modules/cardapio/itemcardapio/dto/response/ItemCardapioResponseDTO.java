package br.com.cardapio.modules.cardapio.itemcardapio.dto.response;

import br.com.cardapio.modules.core.video.dto.response.VideoResponseDTO;
import br.com.cardapio.modules.cardapio.categoria.dto.response.CategoriaResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.model.Especificacao;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Item do cardápio na visão do gestor.
 */
@Schema(description = "Item do cardápio (painel)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemCardapioResponseDTO(

        @Schema(description = "Identificador do item")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome", example = "Cappuccino italiano")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Descrição")
        @JsonProperty("descricao")
        String descricao,

        @Schema(description = "Valor em reais", example = "14.90")
        @JsonProperty("valor")
        BigDecimal valor,

        @Schema(description = "Se aparece no cardápio público")
        @JsonProperty("ativo")
        boolean ativo,

        @Schema(description = "Posição no feed")
        @JsonProperty("ordem")
        int ordem,

        @Schema(description = "Categorias")
        @JsonProperty("categorias")
        List<CategoriaResponseDTO> categorias,

        @Schema(description = "Ingredientes")
        @JsonProperty("ingredientes")
        List<String> ingredientes,

        @Schema(description = "Especificações (grupos de opção única)")
        @JsonProperty("especificacoes")
        List<Especificacao> especificacoes,

        @Schema(description = "Imagens, na ordem do carrossel")
        @JsonProperty("imagens")
        List<MidiaResponseDTO> imagens,

        @Schema(description = "Vídeo")
        @JsonProperty("video")
        VideoResponseDTO video,

        @Schema(description = "Última alteração")
        @JsonProperty("atualizadoEm")
        LocalDateTime atualizadoEm
) {}
