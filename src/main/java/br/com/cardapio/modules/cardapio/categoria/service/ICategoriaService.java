package br.com.cardapio.modules.cardapio.categoria.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.categoria.dto.request.CategoriaRequestDTO;
import br.com.cardapio.modules.cardapio.categoria.dto.response.CategoriaResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Contrato do CRUD de categorias do restaurante do gestor logado.
 */
public interface ICategoriaService {

    /**
     * @param usuarioLogado gestor (define o restaurante)
     * @return categorias ordenadas por ordem e nome
     * @throws BusinessException {@code UNAUTHORIZED} se o usuário não administra restaurante
     */
    List<CategoriaResponseDTO> findAllCategorias(UsuarioUserDetailsDTO usuarioLogado);

    /**
     * @throws BusinessException {@code CATEGORIA_ALREADY_EXISTS} (409) se o nome já existir no restaurante
     */
    CategoriaResponseDTO saveCategoria(CategoriaRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * @throws BusinessException {@code NOT_FOUND} (inclusive se for de outro restaurante) ou {@code CATEGORIA_ALREADY_EXISTS}
     */
    CategoriaResponseDTO updateCategoria(UUID id, CategoriaRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Exclui a categoria; os itens continuam, só perdem o vínculo (FK com ON DELETE CASCADE na tabela de junção).
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    void deleteCategoriaById(UUID id, UsuarioUserDetailsDTO usuarioLogado);
}
