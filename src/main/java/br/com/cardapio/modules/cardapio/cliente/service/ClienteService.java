package br.com.cardapio.modules.cardapio.cliente.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.infrastructure.service.TokenService;
import br.com.cardapio.modules.cardapio.cliente.dto.request.IdentificacaoClienteRequestDTO;
import br.com.cardapio.modules.cardapio.cliente.dto.response.ClienteSessaoResponseDTO;
import br.com.cardapio.modules.cardapio.cliente.model.Cliente;
import br.com.cardapio.modules.cardapio.cliente.repository.IClienteRepository;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Identificação do cliente no cardápio público: um cadastro por CPF em cada restaurante e um token por navegador.
 */
@Service
@RequiredArgsConstructor
public class ClienteService implements IClienteService {

    private final IClienteRepository clienteRepository;
    private final IRestauranteRepository restauranteRepository;
    private final TokenService tokenService;

    @Override
    @Transactional
    public ClienteSessaoResponseDTO identificarCliente(final String slug, final IdentificacaoClienteRequestDTO dto) {
        final Restaurante restaurante = restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));
        final Cliente cliente = clienteRepository.findByRestauranteIdAndCpf(restaurante.getId(), dto.cpf())
                .orElseGet(() -> {
                    final Cliente novo = new Cliente();
                    novo.setRestaurante(restaurante);
                    novo.setCpf(dto.cpf());
                    return novo;
                });
        // ponytail: sem verificação por SMS, quem informa o CPF assume o cadastro (último nome/telefone vale).
        cliente.setNome(dto.nome().trim());
        cliente.setTelefone(dto.telefone());
        final Cliente salvo = clienteRepository.save(cliente);
        return new ClienteSessaoResponseDTO(tokenService.generateTokenCliente(salvo.getId()), salvo.getNome());
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente findClienteByTokenOrThrow(final UUID restauranteId, final String token) {
        return Optional.ofNullable(tokenService.validateTokenCliente(token))
                .flatMap(clienteId -> clienteRepository.findByIdAndRestauranteId(clienteId, restauranteId))
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.CLIENTE_NAO_IDENTIFICADO.getMessage()));
    }
}
