package br.com.cardapio.modules.cardapio.publico.dto.response;

import br.com.cardapio.modules.cardapio.adicional.dto.response.AdicionalResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.model.Especificacao;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Item no feed público. {@code videoHlsUrl} só vem quando a transcodificação terminou.
 */
@Schema(description = "Item do cardápio público")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemPublicoResponseDTO(

        @Schema(description = "Identificador")
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

        @Schema(description = "Categorias do item")
        @JsonProperty("categoriaIds")
        List<UUID> categoriaIds,

        @Schema(description = "Ingredientes")
        @JsonProperty("ingredientes")
        List<String> ingredientes,

        @Schema(description = "Especificações: o cliente escolhe uma opção por grupo")
        @JsonProperty("especificacoes")
        List<Especificacao> especificacoes,

        @Schema(description = "URLs das imagens, na ordem do carrossel")
        @JsonProperty("imagens")
        List<String> imagens,

        @Schema(description = "Playlist HLS mestre")
        @JsonProperty("videoHlsUrl")
        String videoHlsUrl,

        @Schema(description = "Pôster do vídeo")
        @JsonProperty("videoPosterUrl")
        String videoPosterUrl,

        @Schema(description = "Adicionais pagos que o cliente pode somar ao item")
        @JsonProperty("adicionais")
        List<AdicionalResponseDTO> adicionais,

        @Schema(description = "Média das estrelas, com uma casa (ausente sem avaliações)", example = "4.6")
        @JsonProperty("avaliacaoMedia")
        Double avaliacaoMedia,

        @Schema(description = "Quantidade de avaliações", example = "23")
        @JsonProperty("avaliacoes")
        long avaliacoes,

        @Schema(description = "Quantidade de comentários", example = "8")
        @JsonProperty("comentarios")
        long comentarios
) {}
