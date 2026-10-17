package br.com.cardapio.modules.core.usuario.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Identidade da sessão: devolvida no login e em "meus dados". Nunca carrega senha nem token.
 */
@Schema(description = "Dados da sessão do usuário logado")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioSessaoResponseDTO(

        @Schema(description = "Identificador do usuário")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome do usuário", example = "Maria")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "E-mail do usuário", example = "gestor@cafe.com")
        @JsonProperty("email")
        String email,

        @Schema(description = "Nome do perfil", example = "Gestor Restaurante")
        @JsonProperty("perfil")
        String perfil,

        @Schema(description = "Restaurante administrado (ausente para o super admin)")
        @JsonProperty("restauranteId")
        UUID restauranteId,

        @Schema(description = "Nome do restaurante administrado", example = "Café do Zé")
        @JsonProperty("restauranteNome")
        String restauranteNome,

        @Schema(description = "Slug público do restaurante administrado", example = "cafe-do-ze")
        @JsonProperty("restauranteSlug")
        String restauranteSlug,

        @Schema(description = "Permissões do perfil")
        @JsonProperty("permissoes")
        Set<String> permissoes,

        @Schema(description = "Expiração da sessão (só no login)")
        @JsonProperty("expiraEm")
        Instant expiraEm
) {}
