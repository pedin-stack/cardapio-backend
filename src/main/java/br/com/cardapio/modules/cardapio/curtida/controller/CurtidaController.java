package br.com.cardapio.modules.cardapio.curtida.controller;

import br.com.cardapio.modules.cardapio.curtida.service.ICurtidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Curtir/descurtir no cardápio público. O dispositivo vem no header {@code X-Device-ID} (o mesmo do rate limit).
 */
@RestController
@RequestMapping("/api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/curtida")
@Tag(name = "Curtida", description = "Curtidas do cardápio público (aba Curtidos)")
@RequiredArgsConstructor
public class CurtidaController {

    private final ICurtidaService curtidaService;

    /**
     * Rota pública: PUT /api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/curtida
     */
    @Operation(summary = "Curtir item", description = "Idempotente: curtir de novo não duplica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item curtido"),
            @ApiResponse(responseCode = "400", description = "X-Device-ID ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível ou item inexistente")
    })
    @PreAuthorize("permitAll()")
    @PutMapping
    public ResponseEntity<?> saveCurtida(@PathVariable final String slug,
                                         @PathVariable final UUID itemId,
                                         @RequestHeader("X-Device-ID") final UUID dispositivoId) {
        curtidaService.saveCurtida(slug, itemId, dispositivoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rota pública: DELETE /api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/curtida
     */
    @Operation(summary = "Descurtir item", description = "Marca a curtida como removida (conta como 'quase pedido').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Curtida removida"),
            @ApiResponse(responseCode = "400", description = "X-Device-ID ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível")
    })
    @PreAuthorize("permitAll()")
    @DeleteMapping
    public ResponseEntity<?> deleteCurtida(@PathVariable final String slug,
                                           @PathVariable final UUID itemId,
                                           @RequestHeader("X-Device-ID") final UUID dispositivoId) {
        curtidaService.deleteCurtida(slug, itemId, dispositivoId);
        return ResponseEntity.noContent().build();
    }
}
