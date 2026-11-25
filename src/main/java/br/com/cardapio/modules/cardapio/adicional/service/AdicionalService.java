package br.com.cardapio.modules.cardapio.adicional.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.adicional.dto.request.AdicionalRequestDTO;
import br.com.cardapio.modules.cardapio.adicional.dto.response.AdicionalResponseDTO;
import br.com.cardapio.modules.cardapio.adicional.model.Adicional;
import br.com.cardapio.modules.cardapio.adicional.repository.IAdicionalRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.core.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * CRUD de adicionais, sempre restrito ao restaurante do gestor logado.
 */
@Service
@RequiredArgsConstructor
public class AdicionalService implements IAdicionalService {

    private final IAdicionalRepository adicionalRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IRestauranteRepository restauranteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdicionalResponseDTO> findAdicionaisPagina(final Pageable pageable, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        return adicionalRepository.findByRestauranteId(restauranteId, pageable).map(AdicionalService::mapToAdicionalResponseDTO);
    }

    @Override
    @Transactional
    public AdicionalResponseDTO saveAdicional(final AdicionalRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final Adicional adicional = new Adicional();
        adicional.setRestaurante(restauranteRepository.getReferenceById(restauranteId));
        applyAdicionalRequestDTO(adicional, dto, restauranteId);
        return mapToAdicionalResponseDTO(adicionalRepository.save(adicional));
    }

    @Override
    @Transactional
    public AdicionalResponseDTO updateAdicional(final UUID id, final AdicionalRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final Adicional adicional = findAdicionalByIdOrThrow(id, restauranteId);
        applyAdicionalRequestDTO(adicional, dto, restauranteId);
        return mapToAdicionalResponseDTO(adicional);
    }

    @Override
    @Transactional
    public void deleteAdicionalById(final UUID id, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        adicionalRepository.delete(findAdicionalByIdOrThrow(id, restauranteId));
    }

    // ---- Helpers Privados ----

    /** Adicional de outro restaurante responde 404 (não revela que o id existe). */
    private Adicional findAdicionalByIdOrThrow(final UUID id, final UUID restauranteId) {
        return adicionalRepository.findByIdAndRestauranteId(id, restauranteId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage()));
    }

    private void applyAdicionalRequestDTO(final Adicional adicional, final AdicionalRequestDTO dto, final UUID restauranteId) {
        final Set<UUID> itemIds = dto.itemIds() == null ? Set.of() : new HashSet<>(dto.itemIds());
        final List<ItemCardapio> itens = itemIds.isEmpty() ? List.of() : itemCardapioRepository.findByIdInAndRestauranteId(itemIds, restauranteId);
        if (itens.size() != itemIds.size()) {
            throw new BusinessException(BusinessExceptionMessage.ITEM_INVALIDO.getMessage());
        }
        adicional.setNome(dto.nome().trim());
        adicional.setValor(dto.valor());
        adicional.getItens().clear();
        adicional.getItens().addAll(itens);
    }

    private static AdicionalResponseDTO mapToAdicionalResponseDTO(final Adicional adicional) {
        return new AdicionalResponseDTO(adicional.getId(), adicional.getNome(), adicional.getValor(),
                adicional.getItens().stream().map(ItemCardapio::getId).toList());
    }
}
