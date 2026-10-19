package br.com.cardapio.modules.cardapio.cliente.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta da identificação: o token que o navegador guarda e manda em {@code X-Cliente-Token} para comentar e
 * avaliar. Devolve só o nome informado agora (nunca dados já gravados de quem tinha aquele CPF).
 */
@Schema(description = "Cliente identificado no cardápio público")
public record ClienteSessaoResponseDTO(

        @Schema(description = "Token do cliente (vale um ano)")
        @JsonProperty("token")
        String token,

        @Schema(description = "Nome informado", example = "Maria Souza")
        @JsonProperty("nome")
        String nome
) {}
