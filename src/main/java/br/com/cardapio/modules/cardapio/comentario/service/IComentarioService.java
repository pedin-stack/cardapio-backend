package br.com.cardapio.modules.cardapio.comentario.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.comentario.dto.request.ComentarioRequestDTO;
import br.com.cardapio.modules.cardapio.comentario.dto.response.ComentarioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato dos comentários: leitura e escrita no cardápio público, leitura e moderação no painel do gestor.
 */
public interface IComentarioService {

    /**
     * Comentários de um item ativo do cardápio público.
     *
     * @throws BusinessException {@code SERVICO_INDISPONIVEL}/{@code NOT_FOUND} (404)
     */
    Page<ComentarioResponseDTO> findComentariosPublicos(String slug, UUID itemId, Pageable pageable);

    /**
     * @param token valor do header {@code X-Cliente-Token}
     * @throws BusinessException {@code SERVICO_INDISPONIVEL}/{@code NOT_FOUND} (404) ou {@code CLIENTE_NAO_IDENTIFICADO} (401)
     */
    ComentarioResponseDTO saveComentario(String slug, UUID itemId, ComentarioRequestDTO dto, String token);

    /**
     * Gestor: comentários de um item do próprio restaurante (inclusive inativo).
     *
     * @throws BusinessException {@code NOT_FOUND} (inclusive se o item for de outro restaurante)
     */
    Page<ComentarioResponseDTO> findComentariosItem(UUID itemId, Pageable pageable, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Gestor: apaga um comentário de item do próprio restaurante.
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    void deleteComentarioById(UUID id, UsuarioUserDetailsDTO usuarioLogado);
}
