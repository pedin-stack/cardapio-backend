package br.com.cardapio.modules.cardapio.publico.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Dados públicos do restaurante (sem gestor nem caminhos internos).
 */
@Schema(description = "Restaurante no cardápio público")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestaurantePublicoResponseDTO(

        @Schema(description = "Identificador")
        @JsonProperty("id")
        UUID id,

        @Schema(description = "Nome", example = "Café do Zé")
        @JsonProperty("nome")
        String nome,

        @Schema(description = "Slug", example = "cafe-do-ze")
        @JsonProperty("slug")
        String slug,

        @Schema(description = "Descrição")
        @JsonProperty("descricao")
        String descricao,

        @Schema(description = "URL do logo")
        @JsonProperty("logoUrl")
        String logoUrl,

        @Schema(description = "URL da capa (hero)")
        @JsonProperty("capaUrl")
        String capaUrl,

        @Schema(description = "Cor de destaque do cardápio (ausente = padrão)", example = "#e9b872")
        @JsonProperty("corDestaque")
        String corDestaque,

        @Schema(description = "URL da logo horizontal do cabeçalho")
        @JsonProperty("logoCabecalhoUrl")
        String logoCabecalhoUrl,

        @Schema(description = "Playlist HLS do vídeo de fundo da capa (só quando pronto)")
        @JsonProperty("videoHlsUrl")
        String videoHlsUrl,

        @Schema(description = "Pôster do vídeo da capa")
        @JsonProperty("videoPosterUrl")
        String videoPosterUrl,

        @Schema(description = "WhatsApp com DDD, só dígitos", example = "74999998888")
        @JsonProperty("whatsapp")
        String whatsapp,

        @Schema(description = "Usuário do Instagram, sem o @", example = "blackbullirece")
        @JsonProperty("instagram")
        String instagram
) {}
