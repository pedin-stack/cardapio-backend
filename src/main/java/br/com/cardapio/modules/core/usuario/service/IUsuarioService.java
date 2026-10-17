package br.com.cardapio.modules.core.usuario.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.core.usuario.dto.response.UsuarioSessaoResponseDTO;

/**
 * Contrato de consulta do usuário logado.
 */
public interface IUsuarioService {

    /**
     * Identidade, perfil, permissões e restaurante do usuário logado (hidratação da sessão no front).
     *
     * @param usuarioLogado principal do JWT
     * @return dados da sessão
     * @throws BusinessException {@code NOT_FOUND} se o usuário deixou de existir
     */
    UsuarioSessaoResponseDTO findMeusDados(UsuarioUserDetailsDTO usuarioLogado);
}
