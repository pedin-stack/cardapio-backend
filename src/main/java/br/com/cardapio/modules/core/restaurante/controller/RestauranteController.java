package br.com.cardapio.modules.core.restaurante.controller;

import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteAtualizarRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteStatusRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.response.RestauranteResponseDTO;
import br.com.cardapio.modules.core.restaurante.service.IRestauranteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * CRUD de restaurantes da plataforma (super admin).
 */
@RestController
@RequestMapping("/api/v1/core/restaurantes")
@Tag(name = "Restaurante", description = "Gestão dos restaurantes da plataforma")
@RequiredArgsConstructor
public class RestauranteController {

    private final IRestauranteService restauranteService;

    /**
     * Rota: GET /api/v1/core/restaurantes
     */
    @Operation(summary = "Listar restaurantes", description = "Lista paginada, com filtro opcional pelo nome.")
    @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    @PreAuthorize("hasAuthority('CORE_RESTAURANTE_VISUALIZAR_TODOS')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAllRestaurantes(
            @RequestParam(required = false) final String nome,
            @PageableDefault(size = 10, sort = "criadoEm", direction = Sort.Direction.DESC) final Pageable pageable) {
        return ResponseEntity.ok(restauranteService.findAllRestaurantes(nome, pageable));
    }

    /**
     * Rota: GET /api/v1/core/restaurantes/restaurante/{id}
     */
    @Operation(summary = "Buscar restaurante", description = "Detalhe do restaurante com o gestor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado",
                    content = @Content(schema = @Schema(implementation = RestauranteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    @PreAuthorize("hasAuthority('CORE_RESTAURANTE_VISUALIZAR_TODOS')")
    @GetMapping(value = "/restaurante/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findRestauranteById(@PathVariable final UUID id) {
        return ResponseEntity.ok(restauranteService.findRestauranteById(id));
    }

    /**
     * Rota: POST /api/v1/core/restaurantes/restaurante
     */
    @Operation(summary = "Cadastrar restaurante", description = "Cadastra o restaurante e a conta do seu gestor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurante cadastrado",
                    content = @Content(schema = @Schema(implementation = RestauranteResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Slug ou e-mail já em uso"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CORE_RESTAURANTE_CRIAR')")
    @PostMapping(value = "/restaurante", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveRestaurante(@RequestBody @Valid final RestauranteRequestDTO dto, final BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(restauranteService.saveRestaurante(dto));
    }

    /**
     * Rota: PUT /api/v1/core/restaurantes/restaurante/{id}
     */
    @Operation(summary = "Atualizar restaurante", description = "Atualiza os dados e, opcionalmente, a senha do gestor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurante atualizado",
                    content = @Content(schema = @Schema(implementation = RestauranteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado"),
            @ApiResponse(responseCode = "409", description = "Slug já em uso"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CORE_RESTAURANTE_ATUALIZAR')")
    @PutMapping(value = "/restaurante/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateRestaurante(@PathVariable final UUID id,
                                               @RequestBody @Valid final RestauranteAtualizarRequestDTO dto,
                                               final BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.ok(restauranteService.updateRestaurante(id, dto));
    }

    /**
     * Rota: PUT /api/v1/core/restaurantes/restaurante/{id}/status
     */
    @Operation(summary = "Ativar/desativar restaurante", description = "Desativado, o cardápio público responde 'Serviço indisponível'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado",
                    content = @Content(schema = @Schema(implementation = RestauranteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CORE_RESTAURANTE_ATUALIZAR')")
    @PutMapping(value = "/restaurante/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateRestauranteStatus(@PathVariable final UUID id,
                                                     @RequestBody @Valid final RestauranteStatusRequestDTO dto,
                                                     final BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.ok(restauranteService.updateRestauranteStatus(id, dto));
    }

    /**
     * Rota: DELETE /api/v1/core/restaurantes/restaurante/{id}
     */
    @Operation(summary = "Excluir restaurante", description = "Exclui o restaurante, o gestor, o cardápio e as métricas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Restaurante excluído"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    @PreAuthorize("hasAuthority('CORE_RESTAURANTE_APAGAR')")
    @DeleteMapping("/restaurante/{id}")
    public ResponseEntity<?> deleteRestauranteById(@PathVariable final UUID id) {
        restauranteService.deleteRestauranteById(id);
        return ResponseEntity.noContent().build();
    }
}
