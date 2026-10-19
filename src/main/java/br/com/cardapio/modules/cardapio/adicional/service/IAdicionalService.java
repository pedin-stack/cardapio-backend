package br.com.cardapio.modules.cardapio.adicional.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.adicional.dto.request.AdicionalRequestDTO;
import br.com.cardapio.modules.cardapio.adicional.dto.response.AdicionalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato do CRUD de adicionais do restaurante do gestor logado.
 */
public interface IAdicionalService {

    /**
     * @param pageable      página, tamanho e ordem
     * @param usuarioLogado gestor (define o restaurante)
     * @return página de adicionais com os itens ligados
     * @throws BusinessException {@code UNAUTHORIZED} se o usuário não administra restaurante
     */
    Page<AdicionalResponseDTO> findAdicionaisPagina(Pageable pageable, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * @throws BusinessException {@code ITEM_INVALIDO} (400) se algum item não for do restaurante
     */
    AdicionalResponseDTO saveAdicional(AdicionalRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Substitui nome, valor e a lista inteira de itens.
     *
     * @throws BusinessException {@code NOT_FOUND} (inclusive se for de outro restaurante) ou {@code ITEM_INVALIDO}
     */
    AdicionalResponseDTO updateAdicional(UUID id, AdicionalRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Exclui o adicional; os itens continuam (FK com ON DELETE CASCADE na tabela de junção).
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    void deleteAdicionalById(UUID id, UsuarioUserDetailsDTO usuarioLogado);
}
