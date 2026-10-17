package br.com.cardapio.modules.core.usuario.controller;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.modules.core.usuario.dto.response.UsuarioSessaoResponseDTO;
import br.com.cardapio.modules.core.usuario.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dados do usuário logado.
 */
@RestController
@RequestMapping("/api/v1/core/usuarios")
@Tag(name = "Usuário", description = "Dados do usuário logado")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    /**
     * Rota: GET /api/v1/core/usuarios/usuario/meus-dados
     */
    @Operation(summary = "Meus dados", description = "Identidade, perfil, permissões e restaurante do usuário logado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados recuperados",
                    content = @Content(schema = @Schema(implementation = UsuarioSessaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sem sessão")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/usuario/meus-dados", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findMeusDados(@Parameter(hidden = true) @AuthenticationPrincipal final UsuarioUserDetailsDTO usuarioLogado) {
        return ResponseEntity.ok(usuarioService.findMeusDados(usuarioLogado));
    }
}
