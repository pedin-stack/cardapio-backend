package br.com.cardapio.modules.core.restaurante.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cadastro de restaurante junto com a conta do seu gestor (operação única, transacional).
 * Limites espelhados em {@code restaurante.schema.ts} do front.
 */
@Schema(description = "Dados para cadastrar um restaurante e o seu gestor")
public record RestauranteRequestDTO(

        @Schema(description = "Nome do restaurante", example = "Café do Zé")
        @JsonProperty("nome")
        @NotBlank(message = "O nome do restaurante é obrigatório.")
        @Size(max = 120, message = "O nome do restaurante não pode exceder 120 caracteres.")
        String nome,

        @Schema(description = "Endereço público do cardápio (minúsculas, números e hífens)", example = "cafe-do-ze")
        @JsonProperty("slug")
        @NotBlank(message = "O endereço do cardápio é obrigatório.")
        @Size(max = 80, message = "O endereço do cardápio não pode exceder 80 caracteres.")
        @Pattern(regexp = RestauranteAtualizarRequestDTO.SLUG_REGEX, message = "Use só letras minúsculas, números e hífens.")
        String slug,

        @Schema(description = "Descrição curta", example = "Cafés especiais e brunch.")
        @JsonProperty("descricao")
        @Size(max = 500, message = "A descrição não pode exceder 500 caracteres.")
        String descricao,

        @Schema(description = "Caminho do logo no storage", example = "plataforma/imagens/9a7b.webp")
        @JsonProperty("logoPath")
        @Size(max = 500, message = "O caminho do logo não pode exceder 500 caracteres.")
        String logoPath,

        @Schema(description = "Caminho da imagem de capa no storage", example = "plataforma/imagens/1c2d.webp")
        @JsonProperty("capaPath")
        @Size(max = 500, message = "O caminho da capa não pode exceder 500 caracteres.")
        String capaPath,

        @Schema(description = "Nome do gestor", example = "Maria Souza")
        @JsonProperty("gestorNome")
        @NotBlank(message = "O nome do gestor é obrigatório.")
        @Size(max = 100, message = "O nome do gestor não pode exceder 100 caracteres.")
        String gestorNome,

        @Schema(description = "E-mail de login do gestor", example = "maria@cafedoze.com")
        @JsonProperty("gestorEmail")
        @NotBlank(message = "O e-mail do gestor é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 100, message = "O e-mail do gestor não pode exceder 100 caracteres.")
        String gestorEmail,

        @Schema(description = "Senha inicial do gestor", example = "S3nh@Forte")
        @JsonProperty("gestorSenha")
        @NotBlank(message = "A senha do gestor é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String gestorSenha
) {}
