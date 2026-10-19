package br.com.cardapio.modules.cardapio.avaliacao.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.avaliacao.dto.request.AvaliacaoRequestDTO;

import java.util.UUID;

/**
 * Contrato das avaliações do cardápio público.
 */
public interface IAvaliacaoService {

    /**
     * Grava (ou troca) as estrelas do cliente identificado no item.
     *
     * @param token valor do header {@code X-Cliente-Token}
     * @throws BusinessException {@code SERVICO_INDISPONIVEL}/{@code NOT_FOUND} (404) ou {@code CLIENTE_NAO_IDENTIFICADO} (401)
     */
    void saveAvaliacao(String slug, UUID itemId, AvaliacaoRequestDTO dto, String token);
}
