package br.com.cardapio.modules.core.autenticacao.dto.response;

import br.com.cardapio.modules.core.usuario.dto.response.UsuarioSessaoResponseDTO;

import java.time.Instant;

/**
 * Resultado interno do login: o token vai para o cookie HttpOnly e só {@code usuario} vai ao corpo.
 */
public record SessaoDTO(String token, Instant expiraEm, UsuarioSessaoResponseDTO usuario) {
}
