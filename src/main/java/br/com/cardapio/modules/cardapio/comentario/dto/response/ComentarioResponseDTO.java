package br.com.cardapio.modules.cardapio.comentario.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comentário como aparece no cardápio e no painel: só o primeiro nome do autor (telefone e CPF nunca saem).
 */
@Schema(description = "Comentário de um item")
public record ComentarioResponseDTO(

        @Schema(description = "Identificador do comentário")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Primeiro nome do autor", example = "Maria")
        @JsonProperty("autor")
        String autor,

        @Schema(description = "Texto", example = "Picanha no ponto certo!")
        @JsonProperty("texto")
        String texto,

        @Schema(description = "Data do comentário")
        @JsonProperty("criadoEm")
        LocalDateTime criadoEm
) {}
