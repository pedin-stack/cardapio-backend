package br.com.cardapio.modules.core.restaurante.dto.response;

import br.com.cardapio.modules.core.video.dto.response.VideoResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Restaurante na visão do super admin. {@code gestor*} só vem no detalhe e no cadastro.
 */
@Schema(description = "Dados de um restaurante")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestauranteResponseDTO(

        @Schema(description = "Identificador do restaurante")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome", example = "Café do Zé")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Endereço público do cardápio", example = "cafe-do-ze")
        @JsonProperty("slug")
        String slug,

        @Schema(description = "Descrição curta")
        @JsonProperty("descricao")
        String descricao,

        @Schema(description = "Caminho do logo no storage")
        @JsonProperty("logoPath")
        String logoPath,

        @Schema(description = "URL pública do logo")
        @JsonProperty("logoUrl")
        String logoUrl,

        @Schema(description = "Caminho da capa no storage")
        @JsonProperty("capaPath")
        String capaPath,

        @Schema(description = "URL pública da capa")
        @JsonProperty("capaUrl")
        String capaUrl,

        @Schema(description = "Cor de destaque do cardápio (ausente = padrão)", example = "#e9b872")
        @JsonProperty("corDestaque")
        String corDestaque,

        @Schema(description = "Caminho da logo do cabeçalho no storage")
        @JsonProperty("logoCabecalhoPath")
        String logoCabecalhoPath,

        @Schema(description = "URL da logo do cabeçalho")
        @JsonProperty("logoCabecalhoUrl")
        String logoCabecalhoUrl,

        @Schema(description = "Vídeo de fundo da capa")
        @JsonProperty("video")
        VideoResponseDTO video,

        @Schema(description = "WhatsApp com DDD, só dígitos", example = "74999998888")
        @JsonProperty("whatsapp")
        String whatsapp,

        @Schema(description = "Usuário do Instagram, sem o @", example = "blackbullirece")
        @JsonProperty("instagram")
        String instagram,

        @Schema(description = "Se o cardápio está no ar", example = "true")
        @JsonProperty("ativo")
        boolean ativo,

        @Schema(description = "Data de cadastro")
        @JsonProperty("criadoEm")
        LocalDateTime criadoEm,

        @Schema(description = "Nome do gestor")
        @JsonProperty("gestorNome")
        String gestorNome,

        @Schema(description = "E-mail de login do gestor")
        @JsonProperty("gestorEmail")
        String gestorEmail
) {}
