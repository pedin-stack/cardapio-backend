package br.com.cardapio.modules.cardapio.avaliacao.controller;

import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.avaliacao.dto.request.AvaliacaoRequestDTO;
import br.com.cardapio.modules.cardapio.avaliacao.service.IAvaliacaoService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Estrelas dos itens no cardápio público. O cliente vem do header {@code X-Cliente-Token} (identificação).
 */
@RestController
@RequestMapping("/api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/avaliacao")
@Tag(name = "Avaliação", description = "Estrelas dos itens do cardápio público")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final IAvaliacaoService avaliacaoService;

    /**
     * Rota pública: PUT /api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/avaliacao
     */
    @Operation(summary = "Avaliar item", description = "Idempotente: avaliar de novo troca as estrelas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Avaliação gravada"),
            @ApiResponse(responseCode = "401", description = "Cliente não identificado"),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível ou item inexistente"),
            @ApiResponse(responseCode = "422", description = "Estrelas fora de 1 a 5")
    })
    @PreAuthorize("permitAll()")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveAvaliacao(@PathVariable final String slug,
                                           @PathVariable final UUID itemId,
                                           @RequestBody @Valid final AvaliacaoRequestDTO dto,
                                           final BindingResult result,
                                           @RequestHeader(value = "X-Cliente-Token", required = false) final String token) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        avaliacaoService.saveAvaliacao(slug, itemId, dto, token);
        return ResponseEntity.noContent().build();
    }
}
