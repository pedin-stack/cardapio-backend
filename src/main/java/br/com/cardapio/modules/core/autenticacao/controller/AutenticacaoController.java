package br.com.cardapio.modules.core.autenticacao.controller;

import br.com.cardapio.config.security.JWTLoginFilter;
import br.com.cardapio.infrastructure.util.ResultError;
import br.com.cardapio.modules.core.autenticacao.dto.request.LoginRequestDTO;
import br.com.cardapio.modules.core.autenticacao.dto.response.SessaoDTO;
import br.com.cardapio.modules.core.autenticacao.service.IAutenticacaoService;
import br.com.cardapio.modules.core.usuario.dto.response.UsuarioSessaoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

/**
 * Sessão do painel: o JWT é entregue em cookie HttpOnly (o JS do front nunca o vê).
 */
@RestController
@RequestMapping("/api/v1/autenticacao/sessoes")
@Tag(name = "Autenticação", description = "Login e logout do painel")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final IAutenticacaoService autenticacaoService;

    @Value("${api.security.cookie.secure}")
    private boolean cookieSecure;

    /**
     * Login. Rota: POST /api/v1/autenticacao/sessoes/sessao
     */
    @Operation(summary = "Entrar", description = "Valida as credenciais e grava o token no cookie HttpOnly.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sessão criada",
                    content = @Content(schema = @Schema(implementation = UsuarioSessaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuário ou restaurante desativado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/sessao", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveSessao(@RequestBody @Valid final LoginRequestDTO dto, final BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResultError.getResultErrors(result));
        }
        final SessaoDTO sessao = autenticacaoService.login(dto);
        final Duration maxAge = Duration.between(Instant.now(), sessao.expiraEm());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildCookie(sessao.token(), maxAge).toString())
                .body(sessao.usuario());
    }

    /**
     * Logout: expira o cookie (só o Set-Cookie do back consegue apagar um cookie HttpOnly).
     * Rota: DELETE /api/v1/autenticacao/sessoes/sessao
     */
    @Operation(summary = "Sair", description = "Expira o cookie de sessão.")
    @ApiResponse(responseCode = "204", description = "Sessão encerrada")
    @PreAuthorize("permitAll()")
    @DeleteMapping("/sessao")
    public ResponseEntity<?> deleteSessao() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseCookie buildCookie(final String valor, final Duration maxAge) {
        return ResponseCookie.from(JWTLoginFilter.TOKEN_COOKIE_NAME, valor)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
