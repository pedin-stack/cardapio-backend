package br.com.cardapio.modules.cardapio.curtida.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.curtida.model.Curtida;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Curtidas do cardápio público.
 */
@Service
@RequiredArgsConstructor
public class CurtidaService implements ICurtidaService {

    private final ICurtidaRepository curtidaRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IRestauranteRepository restauranteRepository;

    @Override
    @Transactional
    public void saveCurtida(final String slug, final UUID itemId, final UUID dispositivoId) {
        final Restaurante restaurante = findRestauranteAtivoOrThrow(slug);
        if (!itemCardapioRepository.existsByIdAndRestauranteIdAndAtivoTrue(itemId, restaurante.getId())) {
            throw new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage());
        }
        if (curtidaRepository.findByItemIdAndDispositivoIdAndRemovidoEmIsNull(itemId, dispositivoId).isPresent()) {
            return;
        }
        final Curtida curtida = new Curtida();
        curtida.setRestaurante(restaurante);
        curtida.setItem(itemCardapioRepository.getReferenceById(itemId));
        curtida.setDispositivoId(dispositivoId);
        curtida.setCurtidoEm(LocalDateTime.now());
        curtidaRepository.save(curtida);
    }

    @Override
    @Transactional
    public void deleteCurtida(final String slug, final UUID itemId, final UUID dispositivoId) {
        final Restaurante restaurante = findRestauranteAtivoOrThrow(slug);
        curtidaRepository.findByItemIdAndDispositivoIdAndRemovidoEmIsNull(itemId, dispositivoId)
                .filter(curtida -> curtida.getRestaurante().getId().equals(restaurante.getId()))
                .ifPresent(curtida -> curtida.setRemovidoEm(LocalDateTime.now()));
    }

    private Restaurante findRestauranteAtivoOrThrow(final String slug) {
        return restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));
    }
}
