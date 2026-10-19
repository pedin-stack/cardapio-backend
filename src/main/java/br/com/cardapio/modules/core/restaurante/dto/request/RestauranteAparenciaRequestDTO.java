package br.com.cardapio.modules.core.restaurante.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Aparência do cardápio editada pelo gestor no próprio painel. Campo ausente/nulo = sem aquele recurso
 * (cor padrão, sem logo do cabeçalho, sem vídeo, sem rede social). Limites espelhados no front.
 */
@Schema(description = "Aparência do cardápio do restaurante logado")
public record RestauranteAparenciaRequestDTO(

        @Schema(description = "Cor hex (ausente = padrão)", example = "#e9b872")
        @JsonProperty("corDestaque")
        @Pattern(regexp = RestauranteAtualizarRequestDTO.COR_REGEX, message = "Use uma cor no formato #rrggbb.")
        String corDestaque,

        @Schema(description = "Caminho da logo horizontal do cabeçalho no storage", example = "restaurantes/9a7b.../imagens/1c2d.webp")
        @JsonProperty("logoCabecalhoPath")
        @Size(max = 500, message = "O caminho da logo não pode exceder 500 caracteres.")
        String logoCabecalhoPath,

        @Schema(description = "Caminho do vídeo original da capa no storage (vira HLS)", example = "restaurantes/9a7b.../videos/3e4f.mp4")
        @JsonProperty("videoCaminho")
        @Size(max = 500, message = "O caminho do vídeo não pode exceder 500 caracteres.")
        String videoCaminho,

        @Schema(description = "WhatsApp com DDD, só dígitos", example = "74999998888")
        @JsonProperty("whatsapp")
        @Pattern(regexp = "^\\d{10,11}$", message = "Informe o WhatsApp com DDD (10 ou 11 dígitos).")
        String whatsapp,

        @Schema(description = "Usuário do Instagram, sem o @", example = "blackbullirece")
        @JsonProperty("instagram")
        @Pattern(regexp = "^[A-Za-z0-9._]{1,30}$", message = "Use só o usuário do Instagram (letras, números, ponto e _).")
        String instagram
) {}
