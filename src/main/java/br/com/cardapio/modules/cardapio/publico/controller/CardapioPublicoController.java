package br.com.cardapio.modules.cardapio.publico.controller;

import br.com.cardapio.modules.cardapio.publico.dto.response.CardapioPublicoResponseDTO;
import br.com.cardapio.modules.cardapio.publico.service.ICardapioPublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Cardápio público (sem login).
 */
@RestController
@RequestMapping("/api/v1/cardapio/restaurantes")
@Tag(name = "Cardápio público", description = "Cardápio que o cliente final vê")
@RequiredArgsConstructor
public class CardapioPublicoController {

    private final ICardapioPublicoService cardapioPublicoService;

    /**
     * Rota pública: GET /api/v1/cardapio/restaurantes/restaurante/{slug}
     * <p>
     * {@code Cache-Control: max-age=30}: o feed é lido por todo cliente que abre o QR code; meio minuto de atraso
     * para refletir uma edição do gestor é aceitável.
     * </p>
     */
    @Operation(summary = "Cardápio do restaurante", description = "Restaurante, categorias e itens ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cardápio recuperado",
                    content = @Content(schema = @Schema(implementation = CardapioPublicoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Serviço indisponível (restaurante inexistente ou desativado)")
    })
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/restaurante/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findCardapioBySlug(@PathVariable final String slug) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(cardapioPublicoService.findCardapioBySlug(slug));
    }
}
