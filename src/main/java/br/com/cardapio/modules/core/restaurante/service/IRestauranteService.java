package br.com.cardapio.modules.core.restaurante.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteAtualizarRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteCorRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.request.RestauranteStatusRequestDTO;
import br.com.cardapio.modules.core.restaurante.dto.response.RestauranteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato do CRUD de restaurantes (super admin).
 */
public interface IRestauranteService {

    /**
     * Lista restaurantes, filtrando pelo nome.
     *
     * @param nome     trecho do nome (opcional)
     * @param pageable paginação
     * @return página de restaurantes
     */
    Page<RestauranteResponseDTO> findAllRestaurantes(String nome, Pageable pageable);

    /**
     * Detalhe do restaurante com o gestor.
     *
     * @param id identificador
     * @return restaurante
     * @throws BusinessException {@code NOT_FOUND} se não existir
     */
    RestauranteResponseDTO findRestauranteById(UUID id);

    /**
     * Cadastra o restaurante e a conta do gestor na mesma transação.
     *
     * @param dto dados do restaurante e do gestor
     * @return restaurante criado
     * @throws BusinessException {@code SLUG_ALREADY_EXISTS}/{@code EMAIL_ALREADY_EXISTS} (409),
     *                           slug reservado ou caminho de imagem inválido (400)
     */
    RestauranteResponseDTO saveRestaurante(RestauranteRequestDTO dto);

    /**
     * Atualiza os dados do restaurante e, se informada, a senha do gestor.
     *
     * @throws BusinessException {@code NOT_FOUND}, {@code SLUG_ALREADY_EXISTS} ou caminho inválido
     */
    RestauranteResponseDTO updateRestaurante(UUID id, RestauranteAtualizarRequestDTO dto);

    /**
     * Ativa/desativa o restaurante. Desativado: cardápio público 404 e gestor sem acesso.
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    RestauranteResponseDTO updateRestauranteStatus(UUID id, RestauranteStatusRequestDTO dto);

    /**
     * Gestor: dados do próprio restaurante (o id vem da sessão).
     *
     * @throws BusinessException {@code UNAUTHORIZED} se o usuário não tem restaurante
     */
    RestauranteResponseDTO findRestauranteLogado(UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Gestor: muda a cor do cardápio do próprio restaurante (o id vem da sessão).
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    RestauranteResponseDTO updateCorDestaqueRestauranteLogado(UsuarioUserDetailsDTO usuarioLogado, RestauranteCorRequestDTO dto);

    /**
     * Exclui o restaurante, seu gestor e todo o cardápio/métricas (FKs com ON DELETE CASCADE).
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    void deleteRestauranteById(UUID id);
}
