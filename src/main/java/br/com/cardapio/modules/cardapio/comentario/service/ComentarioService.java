package br.com.cardapio.modules.cardapio.comentario.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.cliente.model.Cliente;
import br.com.cardapio.modules.cardapio.cliente.service.IClienteService;
import br.com.cardapio.modules.cardapio.comentario.dto.request.ComentarioRequestDTO;
import br.com.cardapio.modules.cardapio.comentario.dto.response.ComentarioResponseDTO;
import br.com.cardapio.modules.cardapio.comentario.model.Comentario;
import br.com.cardapio.modules.cardapio.comentario.repository.IComentarioRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.core.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comentários dos itens: públicos para ler, só cliente identificado escreve e o gestor modera.
 */
@Service
@RequiredArgsConstructor
public class ComentarioService implements IComentarioService {

    private final IComentarioRepository comentarioRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IRestauranteRepository restauranteRepository;
    private final IClienteService clienteService;

    @Override
    @Transactional(readOnly = true)
    public Page<ComentarioResponseDTO> findComentariosPublicos(final String slug, final UUID itemId, final Pageable pageable) {
        final Restaurante restaurante = findRestauranteAtivoOrThrow(slug);
        validateItemAtivoOrThrow(itemId, restaurante.getId());
        return comentarioRepository.findByItemId(itemId, pageable).map(ComentarioService::mapToComentarioResponseDTO);
    }

    @Override
    @Transactional
    public ComentarioResponseDTO saveComentario(final String slug, final UUID itemId, final ComentarioRequestDTO dto, final String token) {
        final Restaurante restaurante = findRestauranteAtivoOrThrow(slug);
        final Cliente cliente = clienteService.findClienteByTokenOrThrow(restaurante.getId(), token);
        validateItemAtivoOrThrow(itemId, restaurante.getId());
        final Comentario comentario = new Comentario();
        comentario.setItem(itemCardapioRepository.getReferenceById(itemId));
        comentario.setCliente(cliente);
        comentario.setTexto(dto.texto().trim());
        comentario.setCriadoEm(LocalDateTime.now());
        return mapToComentarioResponseDTO(comentarioRepository.save(comentario));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComentarioResponseDTO> findComentariosItem(final UUID itemId, final Pageable pageable, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        if (!itemCardapioRepository.existsByIdAndRestauranteId(itemId, restauranteId)) {
            throw new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage());
        }
        return comentarioRepository.findByItemId(itemId, pageable).map(ComentarioService::mapToComentarioResponseDTO);
    }

    @Override
    @Transactional
    public void deleteComentarioById(final UUID id, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        comentarioRepository.delete(comentarioRepository.findByIdAndItemRestauranteId(id, restauranteId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage())));
    }

    // ---- Helpers Privados ----

    private Restaurante findRestauranteAtivoOrThrow(final String slug) {
        return restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));
    }

    private void validateItemAtivoOrThrow(final UUID itemId, final UUID restauranteId) {
        if (!itemCardapioRepository.existsByIdAndRestauranteIdAndAtivoTrue(itemId, restauranteId)) {
            throw new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage());
        }
    }

    /** Só o primeiro nome do autor vai a público. */
    private static ComentarioResponseDTO mapToComentarioResponseDTO(final Comentario comentario) {
        return new ComentarioResponseDTO(
                comentario.getId(),
                comentario.getCliente().getNome().trim().split("\\s+")[0],
                comentario.getTexto(),
                comentario.getCriadoEm());
    }
}
