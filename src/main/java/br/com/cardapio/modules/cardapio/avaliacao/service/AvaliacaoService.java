package br.com.cardapio.modules.cardapio.avaliacao.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.avaliacao.dto.request.AvaliacaoRequestDTO;
import br.com.cardapio.modules.cardapio.avaliacao.model.Avaliacao;
import br.com.cardapio.modules.cardapio.avaliacao.repository.IAvaliacaoRepository;
import br.com.cardapio.modules.cardapio.cliente.model.Cliente;
import br.com.cardapio.modules.cardapio.cliente.service.IClienteService;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Avaliações do cardápio público (só cliente identificado).
 */
@Service
@RequiredArgsConstructor
public class AvaliacaoService implements IAvaliacaoService {

    private final IAvaliacaoRepository avaliacaoRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IRestauranteRepository restauranteRepository;
    private final IClienteService clienteService;

    @Override
    @Transactional
    public void saveAvaliacao(final String slug, final UUID itemId, final AvaliacaoRequestDTO dto, final String token) {
        final Restaurante restaurante = restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));
        final Cliente cliente = clienteService.findClienteByTokenOrThrow(restaurante.getId(), token);
        if (!itemCardapioRepository.existsByIdAndRestauranteIdAndAtivoTrue(itemId, restaurante.getId())) {
            throw new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage());
        }
        final Avaliacao avaliacao = avaliacaoRepository.findByItemIdAndClienteId(itemId, cliente.getId())
                .orElseGet(() -> {
                    final Avaliacao nova = new Avaliacao();
                    nova.setItem(itemCardapioRepository.getReferenceById(itemId));
                    nova.setCliente(cliente);
                    return nova;
                });
        avaliacao.setEstrelas(dto.estrelas().shortValue());
        avaliacaoRepository.save(avaliacao);
    }
}
