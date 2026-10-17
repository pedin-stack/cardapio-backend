package br.com.cardapio.modules.cardapio.itemcardapio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Arquivo no storage: o caminho (para reenviar no formulário) e a URL pública (para exibir).
 */
@Schema(description = "Arquivo de mídia")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MidiaResponseDTO(

        @Schema(description = "Caminho no storage")
        @JsonProperty("caminho")
        String caminho,

        @Schema(description = "URL pública")
        @JsonProperty("url")
        String url
) {}
