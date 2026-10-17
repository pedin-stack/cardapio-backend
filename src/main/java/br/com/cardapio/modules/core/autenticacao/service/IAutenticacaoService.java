package br.com.cardapio.modules.core.autenticacao.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.core.autenticacao.dto.request.LoginRequestDTO;
import br.com.cardapio.modules.core.autenticacao.dto.response.SessaoDTO;

/**
 * Contrato da autenticação do painel.
 */
public interface IAutenticacaoService {

    /**
     * Valida as credenciais e emite o token.
     *
     * @param dto e-mail e senha
     * @return token, expiração e identidade da sessão
     * @throws BusinessException {@code INVALID_CREDENTIALS} (401) se e-mail/senha não conferem;
     *                           {@code USUARIO_INATIVO} (403) se o usuário ou o restaurante estiverem desativados
     */
    SessaoDTO login(LoginRequestDTO dto);
}
