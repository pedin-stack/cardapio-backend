package br.com.cardapio.modules.cardapio.categoria.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.categoria.dto.request.CategoriaRequestDTO;
import br.com.cardapio.modules.cardapio.categoria.dto.response.CategoriaResponseDTO;
import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import br.com.cardapio.modules.cardapio.categoria.repository.ICategoriaRepository;
import br.com.cardapio.modules.cardapio.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de categorias, sempre restrito ao restaurante do gestor logado.
 */
@Service
@RequiredArgsConstructor
public class CategoriaService implements ICategoriaService {

    private final ICategoriaRepository categoriaRepository;
    private final IRestauranteRepository restauranteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findAllCategorias(final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        return categoriaRepository.findByRestauranteIdOrderByOrdemAscNomeAsc(restauranteId).stream()
                .map(CategoriaService::mapToCategoriaResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> findCategoriasPagina(final Pageable pageable, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        return categoriaRepository.findByRestauranteId(restauranteId, pageable).map(CategoriaService::mapToCategoriaResponseDTO);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO saveCategoria(final CategoriaRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final String nome = dto.nome().trim();
        if (categoriaRepository.existsByRestauranteIdAndNomeIgnoreCase(restauranteId, nome)) {
            throw new BusinessException(BusinessExceptionMessage.CATEGORIA_ALREADY_EXISTS.getMessage());
        }
        final Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setOrdem(dto.ordem() != null ? dto.ordem() : 0);
        categoria.setRestaurante(restauranteRepository.getReferenceById(restauranteId));
        return mapToCategoriaResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO updateCategoria(final UUID id, final CategoriaRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final Categoria categoria = findCategoriaByIdOrThrow(id, restauranteId);
        final String nome = dto.nome().trim();
        if (categoriaRepository.existsByRestauranteIdAndNomeIgnoreCaseAndIdNot(restauranteId, nome, id)) {
            throw new BusinessException(BusinessExceptionMessage.CATEGORIA_ALREADY_EXISTS.getMessage());
        }
        categoria.setNome(nome);
        if (dto.ordem() != null) {
            categoria.setOrdem(dto.ordem());
        }
        return mapToCategoriaResponseDTO(categoria);
    }

    @Override
    @Transactional
    public void deleteCategoriaById(final UUID id, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        categoriaRepository.delete(findCategoriaByIdOrThrow(id, restauranteId));
    }

    // ---- Helpers Privados ----

    /** Categoria de outro restaurante responde 404 (não revela que o id existe). */
    private Categoria findCategoriaByIdOrThrow(final UUID id, final UUID restauranteId) {
        return categoriaRepository.findByIdAndRestauranteId(id, restauranteId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage()));
    }

    public static CategoriaResponseDTO mapToCategoriaResponseDTO(final Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome(), categoria.getOrdem());
    }
}
