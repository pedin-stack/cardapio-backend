package br.com.cardapio.modules.cardapio.itemcardapio.dto.response;

import br.com.cardapio.modules.cardapio.itemcardapio.enums.StatusVideo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Vídeo do item: playlist HLS mestre (chunks de 2 s em 480p/720p) e pôster.
 */
@Schema(description = "Vídeo do item")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VideoResponseDTO(

        @Schema(description = "Situação da transcodificação", example = "PRONTO")
        @JsonProperty("status")
        StatusVideo status,

        @Schema(description = "Caminho do original no storage (só no painel)")
        @JsonProperty("caminhoOriginal")
        String caminhoOriginal,

        @Schema(description = "URL da playlist HLS mestre (master.m3u8)")
        @JsonProperty("hlsUrl")
        String hlsUrl,

        @Schema(description = "URL do pôster (primeiro quadro)")
        @JsonProperty("posterUrl")
        String posterUrl
) {}
