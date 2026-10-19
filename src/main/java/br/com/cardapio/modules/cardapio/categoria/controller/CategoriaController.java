package br.com.cardapio.modules.cardapio.categoria.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.categoria.dto.request.CategoriaRequestDTO;
import br.com.cardapio.modules.cardapio.categoria.dto.response.CategoriaResponseDTO;
import br.com.cardapio.modules.cardapio.categoria.service.ICategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Categorias do cardápio do gestor logado.
 */
@RestController
@RequestMapping("/api/v1/cardapio/categorias")
@Tag(name = "Categoria", description = "Categorias personalizadas do cardápio")
@RequiredArgsConstructor
public class CategoriaController {

    private final ICategoriaService categoriaService;

    /**
     * Rota: GET /api/v1/cardapio/categorias
     */
    @Operation(summary = "Listar categorias", description = "Categorias do restaurante do gestor logado.")
    @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    @PreAuthorize("hasAuthority('CARDAPIO_CATEGORIA_VISUALIZAR_PROPRIAS')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAllCategorias(@Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(categoriaService.findAllCategorias(usuarioLogado));
    }

    /**
     * Rota: GET /api/v1/cardapio/categorias/pagina?page&size
     */
    @Operation(summary = "Listar categorias paginadas", description = "Categorias do restaurante do gestor, por ordem e nome.")
    @ApiResponse(responseCode = "200", description = "Página recuperada com sucesso")
    @PreAuthorize("hasAuthority('CARDAPIO_CATEGORIA_VISUALIZAR_PROPRIAS')")
    @GetMapping(value = "/pagina", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findCategoriasPagina(
            @PageableDefault(size = 10, sort = {"ordem", "nome", "id"}) final Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(categoriaService.findCategoriasPagina(pageable, usuarioLogado));
    }

    /**
     * Rota: POST /api/v1/cardapio/categorias/categoria
     */
    @Operation(summary = "Cadastrar categoria", description = "Cria uma categoria no restaurante do gestor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Nome já existe"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_CATEGORIA_CRIAR')")
    @PostMapping(value = "/categoria", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveCategoria(@RequestBody @Valid final CategoriaRequestDTO dto,
                                           final BindingResult result,
                                           @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.saveCategoria(dto, usuarioLogado));
    }

    /**
     * Rota: PUT /api/v1/cardapio/categorias/categoria/{id}
     */
    @Operation(summary = "Atualizar categoria", description = "Renomeia ou reordena a categoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome já existe"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_CATEGORIA_ATUALIZAR')")
    @PutMapping(value = "/categoria/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCategoria(@PathVariable final UUID id,
                                             @RequestBody @Valid final CategoriaRequestDTO dto,
                                             final BindingResult result,
                                             @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.ok(categoriaService.updateCategoria(id, dto, usuarioLogado));
    }

    /**
     * Rota: DELETE /api/v1/cardapio/categorias/categoria/{id}
     */
    @Operation(summary = "Excluir categoria", description = "Exclui a categoria; os itens só perdem o vínculo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_CATEGORIA_APAGAR')")
    @DeleteMapping("/categoria/{id}")
    public ResponseEntity<?> deleteCategoriaById(@PathVariable final UUID id,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        categoriaService.deleteCategoriaById(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
