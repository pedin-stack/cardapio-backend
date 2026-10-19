package br.com.cardapio.modules.cardapio.metrica.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.enums.OrdemRanking;
import br.com.cardapio.modules.cardapio.metrica.enums.TipoRanking;
import br.com.cardapio.modules.cardapio.metrica.service.IMetricaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Métricas do cardápio do gestor logado.
 */
@RestController
@RequestMapping("/api/v1/cardapio/metricas")
@Tag(name = "Métricas", description = "Tempo por item, curtidas, quase pedidos e horários")
@RequiredArgsConstructor
public class MetricaController {

    private final IMetricaService metricaService;

    /**
     * Rota: GET /api/v1/cardapio/metricas?inicio=AAAA-MM-DD&fim=AAAA-MM-DD&itemId=
     */
    @Operation(summary = "Métricas do período", description = "Padrão: últimos 30 dias. itemId filtra o mapa de calor de horários.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas calculadas",
                    content = @Content(schema = @Schema(implementation = MetricasResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_METRICA_VISUALIZAR_PROPRIAS')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fim,
            @RequestParam(required = false) final UUID itemId,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(metricaService.findMetricas(inicio, fim, itemId, usuarioLogado));
    }

    /**
     * Rota: GET /api/v1/cardapio/metricas/ranking?tipo=CURTIDOS&inicio&fim&categoriaId&ordem=MAIOR&page&size
     */
    @Operation(summary = "Ranking de itens", description = "Mais curtidos, quase pedidos ou tempo médio de tela, paginado, por categoria e ordem.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página do ranking"),
            @ApiResponse(responseCode = "400", description = "Período inválido")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_METRICA_VISUALIZAR_PROPRIAS')")
    @GetMapping(value = "/ranking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findRanking(
            @RequestParam final TipoRanking tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fim,
            @RequestParam(required = false) final UUID categoriaId,
            @RequestParam(defaultValue = "MAIOR") final OrdemRanking ordem,
            @PageableDefault(size = 10) final Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(metricaService.findRanking(tipo, inicio, fim, categoriaId, ordem, pageable, usuarioLogado));
    }
}
