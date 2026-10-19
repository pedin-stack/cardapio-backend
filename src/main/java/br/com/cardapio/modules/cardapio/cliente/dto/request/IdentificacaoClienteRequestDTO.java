package br.com.cardapio.modules.cardapio.cliente.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

/**
 * Identificação do cliente no cardápio público. Telefone e CPF chegam só com dígitos (o front tira a máscara);
 * o {@code @CPF} confere os dígitos verificadores.
 */
@Schema(description = "Identificação do cliente no cardápio público")
public record IdentificacaoClienteRequestDTO(

        @Schema(description = "Nome", example = "Maria Souza")
        @JsonProperty("nome")
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 120, message = "O nome não pode exceder 120 caracteres.")
        String nome,

        @Schema(description = "Telefone com DDD, só dígitos", example = "74999998888")
        @JsonProperty("telefone")
        @NotBlank(message = "Informe seu telefone.")
        @Pattern(regexp = "^\\d{10,11}$", message = "Informe o telefone com DDD.")
        String telefone,

        @Schema(description = "CPF, só dígitos", example = "52998224725")
        @JsonProperty("cpf")
        @NotBlank(message = "Informe seu CPF.")
        @Pattern(regexp = "^\\d{11}$", message = "O CPF deve ter 11 dígitos.")
        @CPF(message = "CPF inválido.")
        String cpf
) {}
