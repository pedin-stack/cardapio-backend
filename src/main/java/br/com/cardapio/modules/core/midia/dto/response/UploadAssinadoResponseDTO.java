package br.com.cardapio.modules.core.midia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Destino e token do upload assinado ({@code supabase.storage.from(bucket).uploadToSignedUrl(caminho, token, arquivo)}).
 * Não traz URL de leitura: o bucket é privado e o arquivo ainda não existe — a prévia no front usa o arquivo local,
 * e as respostas de item/restaurante trazem URLs assinadas depois de salvo.
 */
@Schema(description = "Upload assinado")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadAssinadoResponseDTO(

        @Schema(description = "Caminho do objeto no bucket (é o que se envia de volta no item/restaurante)",
                example = "restaurantes/2f1c.../imagens/9a7b....webp")
        @JsonProperty("caminho")
        String caminho,

        @Schema(description = "Token de uso único do upload")
        @JsonProperty("token")
        String token
) {}
