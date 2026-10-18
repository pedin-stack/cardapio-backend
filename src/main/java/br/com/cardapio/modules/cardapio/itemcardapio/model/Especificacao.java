package br.com.cardapio.modules.cardapio.itemcardapio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Grupo de opção única do item ("Ponto da carne": mal passado / ao ponto / bem passado). O cliente escolhe uma
 * opção por grupo. Gravado no JSONB {@code itens_cardapio.especificacoes}; o mesmo record serve de request e
 * response (o formulário envia o item inteiro). Limites espelhados em {@code itemCardapio.schema.ts} do front.
 */
@Schema(description = "Grupo de opções do item (o cliente escolhe uma)")
public record Especificacao(

        @Schema(description = "Nome do grupo", example = "Ponto da carne")
        @JsonProperty("nome")
        @NotBlank(message = "Dê um nome à especificação.")
        @Size(max = 60, message = "O nome da especificação pode ter no máximo 60 caracteres.")
        String nome,

        @Schema(description = "Opções, na ordem de exibição", example = "[\"Mal passado\", \"Ao ponto\", \"Bem passado\"]")
        @JsonProperty("opcoes")
        @Size(min = 2, max = 10, message = "Cada especificação precisa de 2 a 10 opções.")
        List<@NotBlank(message = "Opção vazia.") @Size(max = 60, message = "Cada opção pode ter no máximo 60 caracteres.") String> opcoes
) {}
