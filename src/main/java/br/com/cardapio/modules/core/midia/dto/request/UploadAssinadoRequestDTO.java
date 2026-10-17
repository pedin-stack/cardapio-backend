package br.com.cardapio.modules.core.midia.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Pedido de URL assinada para enviar um arquivo direto ao storage.
 */
@Schema(description = "Pedido de upload assinado")
public record UploadAssinadoRequestDTO(

        @Schema(description = "MIME do arquivo (imagem jpeg/png/webp ou vídeo mp4/quicktime/webm)", example = "image/webp")
        @JsonProperty("tipoConteudo")
        @NotBlank(message = "O tipo do arquivo é obrigatório.")
        String tipoConteudo
) {}
