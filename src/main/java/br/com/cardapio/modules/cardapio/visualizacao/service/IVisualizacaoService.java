package br.com.cardapio.modules.cardapio.visualizacao.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.visualizacao.dto.request.VisualizacoesRequestDTO;

import java.util.UUID;

/**
 * Contrato do registro de tempo de tela por item.
 */
public interface IVisualizacaoService {

    /**
     * Registra o lote. Itens de outro restaurante são ignorados; durações abaixo de 500 ms (rolagem) são
     * descartadas e acima de 10 min são cortadas (aba esquecida aberta).
     *
     * @throws BusinessException {@code SERVICO_INDISPONIVEL} (404) se o restaurante estiver desativado
     */
    void saveVisualizacoes(String slug, VisualizacoesRequestDTO dto, UUID dispositivoId);
}
