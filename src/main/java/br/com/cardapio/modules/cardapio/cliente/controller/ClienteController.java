package br.com.cardapio.modules.cardapio.cliente.controller;

import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.cardapio.cliente.dto.request.IdentificacaoClienteRequestDTO;
import br.com.cardapio.modules.cardapio.cliente.dto.response.ClienteSessaoResponseDTO;
import br.com.cardapio.modules.cardapio.cliente.service.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Identificação do cliente no cardápio público (para comentar e avaliar).
 */
@RestController
@RequestMapping("/api/v1/cardapio/restaurantes/restaurante/{slug}/clientes")
@Tag(name = "Cliente", description = "Identificação do cliente no cardápio público")
@RequiredArgsConstructor
public class ClienteController {

    private final IClienteService clienteService;

    /**
     * Rota pública: POST /api/v1/cardapio/restaurantes/restaurante/{slug}/clientes/cliente
     */
    @Operation(summary = "Identificar cliente", description = "Grava nome, telefone e CPF e devolve o token do cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente identificado",
                    content = @Content(schema = @Schema(implementation = ClienteSessaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante indisponível"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/cliente", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> identificarCliente(@PathVariable final String slug,
                                                @RequestBody @Valid final IdentificacaoClienteRequestDTO dto,
                                                final BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.identificarCliente(slug, dto));
    }
}
