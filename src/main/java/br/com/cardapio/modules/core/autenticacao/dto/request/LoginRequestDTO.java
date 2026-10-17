package br.com.cardapio.modules.core.autenticacao.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciais de login do painel. Espelhado em {@code login.schema.ts} do front.
 */
@Schema(description = "Credenciais de login")
public record LoginRequestDTO(

        @Schema(description = "E-mail do usuário", example = "gestor@cafe.com")
        @JsonProperty("email")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 100, message = "O e-mail não pode exceder 100 caracteres.")
        String email,

        @Schema(description = "Senha", example = "S3nh@Forte")
        @JsonProperty("senha")
        @NotBlank(message = "A senha é obrigatória.")
        @Size(max = 72, message = "A senha não pode exceder 72 caracteres.")
        String senha
) {}
