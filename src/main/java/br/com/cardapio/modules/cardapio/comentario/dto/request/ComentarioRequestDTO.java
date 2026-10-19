package br.com.cardapio.modules.cardapio.comentario.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Comentário do cliente identificado num item.
 */
@Schema(description = "Comentário de um item")
public record ComentarioRequestDTO(

        @Schema(description = "Texto do comentário", example = "Picanha no ponto certo!")
        @JsonProperty("texto")
        @NotBlank(message = "Escreva o comentário.")
        @Size(max = 500, message = "O comentário pode ter no máximo 500 caracteres.")
        String texto
) {}
