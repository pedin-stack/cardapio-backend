package br.com.cardapio.modules.cardapio.adicional.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.adicional.dto.request.AdicionalRequestDTO;
import br.com.cardapio.modules.cardapio.adicional.dto.response.AdicionalResponseDTO;
import br.com.cardapio.modules.cardapio.adicional.service.IAdicionalService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Adicionais pagos do cardápio do gestor logado.
 * ponytail: usa as permissões de item (o adicional faz parte da gestão do item); permissão própria quando houver
 * perfis que editam um e não o outro.
 */
@RestController
@RequestMapping("/api/v1/cardapio/adicionais")
@Tag(name = "Adicional", description = "Adicionais pagos dos itens do cardápio")
@RequiredArgsConstructor
public class AdicionalController {

    private final IAdicionalService adicionalService;

    /**
     * Rota: GET /api/v1/cardapio/adicionais?page&size
     */
    @Operation(summary = "Listar adicionais", description = "Adicionais do restaurante do gestor, os mais novos primeiro.")
    @ApiResponse(responseCode = "200", description = "Página recuperada com sucesso")
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_VISUALIZAR_PROPRIOS')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAdicionaisPagina(
            @PageableDefault(size = 10, sort = {"criadoEm", "id"}, direction = Sort.Direction.DESC) final Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(adicionalService.findAdicionaisPagina(pageable, usuarioLogado));
    }

    /**
     * Rota: POST /api/v1/cardapio/adicionais/adicional
     */
    @Operation(summary = "Cadastrar adicional", description = "Cria um adicional e o liga aos itens informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Adicional criado",
                    content = @Content(schema = @Schema(implementation = AdicionalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Item de outro restaurante"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_CRIAR')")
    @PostMapping(value = "/adicional", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveAdicional(@RequestBody @Valid final AdicionalRequestDTO dto,
                                           final BindingResult result,
                                           @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionalService.saveAdicional(dto, usuarioLogado));
    }

    /**
     * Rota: PUT /api/v1/cardapio/adicionais/adicional/{id}
     */
    @Operation(summary = "Atualizar adicional", description = "Substitui nome, valor e os itens do adicional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adicional atualizado",
                    content = @Content(schema = @Schema(implementation = AdicionalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Item de outro restaurante"),
            @ApiResponse(responseCode = "404", description = "Adicional não encontrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_ATUALIZAR')")
    @PutMapping(value = "/adicional/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAdicional(@PathVariable final UUID id,
                                             @RequestBody @Valid final AdicionalRequestDTO dto,
                                             final BindingResult result,
                                             @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.ok(adicionalService.updateAdicional(id, dto, usuarioLogado));
    }

    /**
     * Rota: DELETE /api/v1/cardapio/adicionais/adicional/{id}
     */
    @Operation(summary = "Excluir adicional", description = "Exclui o adicional; os itens só perdem o vínculo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Adicional excluído"),
            @ApiResponse(responseCode = "404", description = "Adicional não encontrado")
    })
    @PreAuthorize("hasAuthority('CARDAPIO_ITEM_APAGAR')")
    @DeleteMapping("/adicional/{id}")
    public ResponseEntity<?> deleteAdicionalById(@PathVariable final UUID id,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        adicionalService.deleteAdicionalById(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
