package br.com.cardapio.modules.cardapio.itemcardapio.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.request.ItemCardapioRequestDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.response.ItemCardapioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Contrato do CRUD de itens do cardápio do gestor logado.
 */
public interface IItemCardapioService {

    /**
     * @param usuarioLogado gestor (define o restaurante)
     * @return itens (ativos e inativos) na ordem do feed
     * @throws BusinessException {@code UNAUTHORIZED} se o usuário não administra restaurante
     */
    List<ItemCardapioResponseDTO> findAllItensCardapio(UsuarioUserDetailsDTO usuarioLogado);

    /**
     * @param categoriaIds  só itens em alguma destas categorias; vazio ou nulo = todos
     * @param pageable      página, tamanho e ordem
     * @param usuarioLogado gestor (define o restaurante)
     * @return página de itens (ativos e inativos), com as fotos só desta página assinadas
     * @throws BusinessException {@code UNAUTHORIZED} se o usuário não administra restaurante
     */
    Page<ItemCardapioResponseDTO> findItensCardapioPagina(List<UUID> categoriaIds, Pageable pageable, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * @throws BusinessException {@code NOT_FOUND} (inclusive se for de outro restaurante)
     */
    ItemCardapioResponseDTO findItemCardapioById(UUID id, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Cadastra o item. Com {@code videoCaminho}, o item nasce com vídeo PROCESSANDO e a transcodificação
     * HLS roda em segundo plano.
     *
     * @throws BusinessException {@code CATEGORIA_INVALIDA} ou {@code CAMINHO_ARQUIVO_INVALIDO} (400)
     */
    ItemCardapioResponseDTO saveItemCardapio(ItemCardapioRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Atualiza o item inteiro. Trocar {@code videoCaminho} reinicia a transcodificação; omiti-lo remove o vídeo.
     *
     * @throws BusinessException {@code NOT_FOUND}, {@code CATEGORIA_INVALIDA} ou {@code CAMINHO_ARQUIVO_INVALIDO}
     */
    ItemCardapioResponseDTO updateItemCardapio(UUID id, ItemCardapioRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Exclui o item com suas curtidas e visualizações (FKs com ON DELETE CASCADE).
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    void deleteItemCardapioById(UUID id, UsuarioUserDetailsDTO usuarioLogado);
}
