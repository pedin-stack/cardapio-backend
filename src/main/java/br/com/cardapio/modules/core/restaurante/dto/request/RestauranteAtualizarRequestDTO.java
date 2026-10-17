package br.com.cardapio.modules.core.restaurante.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Atualização dos dados do restaurante. {@code gestorSenha} é opcional: preenchida, redefine a senha do gestor
 * (não há recuperação de senha por e-mail no IAM enxuto).
 */
@Schema(description = "Dados para atualizar um restaurante")
public record RestauranteAtualizarRequestDTO(

        @Schema(description = "Nome do restaurante", example = "Café do Zé")
        @JsonProperty("nome")
        @NotBlank(message = "O nome do restaurante é obrigatório.")
        @Size(max = 120, message = "O nome do restaurante não pode exceder 120 caracteres.")
        String nome,

        @Schema(description = "Endereço público do cardápio", example = "cafe-do-ze")
        @JsonProperty("slug")
        @NotBlank(message = "O endereço do cardápio é obrigatório.")
        @Size(max = 80, message = "O endereço do cardápio não pode exceder 80 caracteres.")
        @Pattern(regexp = SLUG_REGEX, message = "Use só letras minúsculas, números e hífens.")
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

        @Schema(description = "Nova senha do gestor (opcional)", example = "N0v@Senha")
        @JsonProperty("gestorSenha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String gestorSenha
) {
    /** Slug: minúsculas/números separados por hífen simples. Espelhado no front. */
    public static final String SLUG_REGEX = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
}
