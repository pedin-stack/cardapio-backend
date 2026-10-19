package br.com.cardapio.modules.cardapio.cliente.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.cliente.dto.request.IdentificacaoClienteRequestDTO;
import br.com.cardapio.modules.cardapio.cliente.dto.response.ClienteSessaoResponseDTO;
import br.com.cardapio.modules.cardapio.cliente.model.Cliente;

import java.util.UUID;

/**
 * Contrato da identificação do cliente no cardápio público.
 */
public interface IClienteService {

    /**
     * Cadastra o cliente (ou atualiza nome e telefone de quem já usou aquele CPF no restaurante) e emite o token.
     *
     * @throws BusinessException {@code SERVICO_INDISPONIVEL} (404) se o restaurante não estiver ativo
     */
    ClienteSessaoResponseDTO identificarCliente(String slug, IdentificacaoClienteRequestDTO dto);

    /**
     * Cliente dono do token, desde que seja do restaurante informado.
     *
     * @param token valor do header {@code X-Cliente-Token} (pode ser nulo)
     * @throws BusinessException {@code CLIENTE_NAO_IDENTIFICADO} (401) se o token faltar, for inválido ou de outro restaurante
     */
    Cliente findClienteByTokenOrThrow(UUID restauranteId, String token);
}
