package br.com.cardapio.modules.cardapio.comentario.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.comentario.dto.request.ComentarioRequestDTO;
import br.com.cardapio.modules.cardapio.comentario.dto.response.ComentarioResponseDTO;
import br.com.cardapio.modules.cardapio.comentario.service.IComentarioService;
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
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Comentários dos itens: rotas públicas do cardápio (ler; escrever com {@code X-Cliente-Token}) e rotas do gestor
 * (ler e apagar). ponytail: a moderação usa as permissões de item.
 */
@RestController
@RequestMapping("/api/v1/cardapio")
@Tag(name = "Comentário", description = "Comentários dos itens do cardápio")
@RequiredArgsConstructor
public class ComentarioController {

    private final IComentarioService comentarioService;

    /**
     * Rota pública: GET /api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/comentarios?page&size
     */
    @Operation(summary = "Listar comentários do item", description = "Os mais novos primeiro, com o primeiro nome do autor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível ou item inexistente")
    })
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/restaurantes/restaurante/{slug}/itens/item/{itemId}/comentarios", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findComentariosPublicos(
            @PathVariable final String slug,
            @PathVariable final UUID itemId,
            @PageableDefault(size = 20, sort = {"criadoEm", "id"}, direction = Sort.Direction.DESC) final Pageable pageable) {
        return ResponseEntity.ok(comentarioService.findComentariosPublicos(slug, itemId, pageable));
    }

    /**
     * Rota pública: POST /api/v1/cardapio/restaurantes/restaurante/{slug}/itens/item/{itemId}/comentarios/comentario
     */
    @Operation(summary = "Comentar item", description = "Só o cliente identificado (header X-Cliente-Token).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentário publicado",
                    content = @Content(schema = @Schema(implementation = ComentarioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Cliente não identificado"),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível ou item inexistente"),
            @ApiResponse(responseCode = "422", description = "Texto vazio ou longo demais")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/restaurantes/restaurante/{slug}/itens/item/{itemId}/comentarios/comentario",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveComentario(@PathVariable final String slug,
                                            @PathVariable final UUID itemId,
                                            @RequestBody @Valid final ComentarioRequestDTO dto,
                                            final BindingResult result,
                                            @RequestHeader(value = "X-Cliente-Token", required = false) final String token) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.saveComentario(slug, itemId, dto, token));
    }

    /**
     * Rota: GET /api/v1/cardapio/itens/item/{itemId}/comentarios?page&size
     */
    @Operation(summary = "Comentários do meu item", description = "Gestor: comentários de um item do próprio restaurante.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página recuperada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_VISUALIZAR_PROPRIOS')")
    @GetMapping(value = "/itens/item/{itemId}/comentarios", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findComentariosItem(
            @PathVariable final UUID itemId,
            @PageableDefault(size = 10, sort = {"criadoEm", "id"}, direction = Sort.Direction.DESC) final Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(comentarioService.findComentariosItem(itemId, pageable, usuarioLogado));
    }

    /**
     * Rota: DELETE /api/v1/cardapio/comentarios/comentario/{id}
     */
    @Operation(summary = "Apagar comentário", description = "Gestor: modera os comentários dos próprios itens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comentário apagado"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_ATUALIZAR')")
    @DeleteMapping("/comentarios/comentario/{id}")
    public ResponseEntity<?> deleteComentarioById(@PathVariable final UUID id,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        comentarioService.deleteComentarioById(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
