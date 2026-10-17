package br.com.cardapio.modules.core.midia.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.core.midia.dto.request.UploadAssinadoRequestDTO;
import br.com.cardapio.modules.core.midia.dto.response.UploadAssinadoResponseDTO;
import br.com.cardapio.modules.core.midia.service.IMidiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Uploads assinados: o arquivo vai do navegador direto ao bucket; a API só guarda o caminho.
 */
@RestController
@RequestMapping("/api/v1/core/midias")
@Tag(name = "Mídia", description = "Uploads assinados para o storage")
@RequiredArgsConstructor
public class MidiaController {

    private final IMidiaService midiaService;

    /**
     * Rota: POST /api/v1/core/midias/upload-assinado
     */
    @Operation(summary = "Gerar upload assinado", description = "Gera caminho e token de uso único para enviar uma imagem ou vídeo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Upload assinado gerado",
                    content = @Content(schema = @Schema(implementation = UploadAssinadoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Tipo de arquivo não suportado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CORE_MIDIA_CRIAR')")
    @PostMapping(value = "/upload-assinado", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveUploadAssinado(@RequestBody @Valid final UploadAssinadoRequestDTO dto,
                                                final BindingResult result,
                                                @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(midiaService.saveUploadAssinado(dto, usuarioLogado));
    }
}
