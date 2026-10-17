package br.com.cardapio.modules.cardapio.itemcardapio.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.request.ItemCardapioRequestDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.response.ItemCardapioResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.service.IItemCardapioService;
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
 * Itens do cardápio do gestor logado.
 */
@RestController
@RequestMapping("/api/v1/cardapio/itens")
@Tag(name = "Item do cardápio", description = "Pratos, imagens e vídeo do cardápio")
@RequiredArgsConstructor
public class ItemCardapioController {

    private final IItemCardapioService itemCardapioService;

    /**
     * Rota: GET /api/v1/cardapio/itens
     */
    @Operation(summary = "Listar itens", description = "Itens (ativos e inativos) do restaurante do gestor, na ordem do feed.")
    @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_VISUALIZAR_PROPRIOS')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAllItensCardapio(@Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(itemCardapioService.findAllItensCardapio(usuarioLogado));
    }

    /**
     * Rota: GET /api/v1/cardapio/itens/item/{id}
     */
    @Operation(summary = "Buscar item", description = "Detalhe do item (usado também no polling do status do vídeo).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item encontrado",
                    content = @Content(schema = @Schema(implementation = ItemCardapioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_VISUALIZAR_PROPRIOS')")
    @GetMapping(value = "/item/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findItemCardapioById(@PathVariable final UUID id,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(itemCardapioService.findItemCardapioById(id, usuarioLogado));
    }

    /**
     * Rota: POST /api/v1/cardapio/itens/item
     */
    @Operation(summary = "Cadastrar item", description = "Cria o item; com vídeo, a transcodificação HLS roda em segundo plano.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item criado",
                    content = @Content(schema = @Schema(implementation = ItemCardapioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Categoria ou caminho de mídia inválido"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_CRIAR')")
    @PostMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveItemCardapio(@RequestBody @Valid final ItemCardapioRequestDTO dto,
                                              final BindingResult result,
                                              @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(itemCardapioService.saveItemCardapio(dto, usuarioLogado));
    }

    /**
     * Rota: PUT /api/v1/cardapio/itens/item/{id}
     */
    @Operation(summary = "Atualizar item", description = "Atualiza o item inteiro; trocar o vídeo reinicia a transcodificação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item atualizado",
                    content = @Content(schema = @Schema(implementation = ItemCardapioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Categoria ou caminho de mídia inválido"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_ATUALIZAR')")
    @PutMapping(value = "/item/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateItemCardapio(@PathVariable final UUID id,
                                                @RequestBody @Valid final ItemCardapioRequestDTO dto,
                                                final BindingResult result,
                                                @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.ok(itemCardapioService.updateItemCardapio(id, dto, usuarioLogado));
    }

    /**
     * Rota: DELETE /api/v1/cardapio/itens/item/{id}
     */
    @Operation(summary = "Excluir item", description = "Exclui o item com suas curtidas e visualizações.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item excluído"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_APAGAR')")
    @DeleteMapping("/item/{id}")
    public ResponseEntity<?> deleteItemCardapioById(@PathVariable final UUID id,
                                                    @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        itemCardapioService.deleteItemCardapioById(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
