package br.com.cardapio.modules.cardapio.curtida.service;

import br.com.cardapio.infrastructure.exception.BusinessException;

import java.util.UUID;

/**
 * Contrato de curtir/descurtir (cliente anônimo identificado pelo dispositivo).
 */
public interface ICurtidaService {

    /**
     * Curte o item. Idempotente: já curtido pelo dispositivo, não cria outra curtida.
     *
     * @param slug          restaurante
     * @param itemId        item curtido
     * @param dispositivoId identificador anônimo do navegador
     * @throws BusinessException {@code SERVICO_INDISPONIVEL} (404) se o restaurante estiver desativado;
     *                           {@code NOT_FOUND} se o item não for do restaurante ou estiver inativo
     */
    void saveCurtida(String slug, UUID itemId, UUID dispositivoId);

    /**
     * Descurte o item (marca {@code removidoEm} — vira "quase pedido"). Idempotente: sem curtida ativa, não faz nada.
     *
     * @throws BusinessException {@code SERVICO_INDISPONIVEL} (404) se o restaurante estiver desativado
     */
    void deleteCurtida(String slug, UUID itemId, UUID dispositivoId);
}
