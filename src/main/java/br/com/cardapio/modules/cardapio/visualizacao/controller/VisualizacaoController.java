package br.com.cardapio.modules.cardapio.visualizacao.controller;

import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.visualizacao.dto.request.VisualizacoesRequestDTO;
import br.com.cardapio.modules.cardapio.visualizacao.service.IVisualizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Tempo de tela por item, enviado em lote pelo cardápio público.
 */
@RestController
@RequestMapping("/api/v1/cardapio/restaurantes/restaurante/{slug}/visualizacoes")
@Tag(name = "Visualização", description = "Tempo gasto em cada item do cardápio")
@RequiredArgsConstructor
public class VisualizacaoController {

    private final IVisualizacaoService visualizacaoService;

    /**
     * Rota pública: POST /api/v1/cardapio/restaurantes/restaurante/{slug}/visualizacoes
     */
    @Operation(summary = "Registrar visualizações", description = "Registra um lote de tempos de tela.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lote registrado"),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível"),
            @ApiResponse(responseCode = "422", description = "Lote inválido")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveVisualizacoes(@PathVariable final String slug,
                                               @RequestBody @Valid final VisualizacoesRequestDTO dto,
                                               final BindingResult result,
                                               @RequestHeader("X-Device-ID") final UUID dispositivoId) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        visualizacaoService.saveVisualizacoes(slug, dto, dispositivoId);
        return ResponseEntity.noContent().build();
    }
}
