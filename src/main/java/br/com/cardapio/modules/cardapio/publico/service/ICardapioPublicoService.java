package br.com.cardapio.modules.cardapio.publico.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.publico.dto.response.CardapioPublicoResponseDTO;

/**
 * Contrato do cardápio público (cliente final, sem login).
 */
public interface ICardapioPublicoService {

    /**
     * @param slug endereço público do restaurante
     * @return restaurante, categorias com itens e itens ativos
     * @throws BusinessException {@code SERVICO_INDISPONIVEL} (404) se o restaurante não existir ou estiver desativado
     */
    CardapioPublicoResponseDTO findCardapioBySlug(String slug);
}
