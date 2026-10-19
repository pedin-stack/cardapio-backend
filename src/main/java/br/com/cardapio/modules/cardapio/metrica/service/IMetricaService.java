package br.com.cardapio.modules.cardapio.metrica.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.dto.response.RankingItemResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.enums.OrdemRanking;
import br.com.cardapio.modules.cardapio.metrica.enums.TipoRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato das métricas do restaurante do gestor logado.
 */
public interface IMetricaService {

    /**
     * Totais do período (visualizações, tempo, curtidas, quase pedidos) e o mapa de calor de horários.
     *
     * @param inicio        início (padrão: 30 dias atrás)
     * @param fim           fim, inclusive (padrão: hoje)
     * @param itemId        filtra o mapa de calor por um item (opcional)
     * @param usuarioLogado gestor (define o restaurante)
     * @return métricas do período
     * @throws BusinessException {@code UNAUTHORIZED} sem restaurante; período invertido ou acima de 366 dias (400)
     */
    MetricasResponseDTO findMetricas(LocalDate inicio, LocalDate fim, UUID itemId, UsuarioUserDetailsDTO usuarioLogado);

    /**
     * Ranking paginado de itens no período.
     *
     * @param tipo          curtidos, quase pedidos ou tempo médio de tela
     * @param inicio        início (padrão: 30 dias atrás)
     * @param fim           fim, inclusive (padrão: hoje)
     * @param categoriaId   só itens desta categoria (opcional)
     * @param ordem         maior valor, menor valor ou nome
     * @param pageable      página e tamanho (o sort é ignorado)
     * @param usuarioLogado gestor (define o restaurante)
     * @return página do ranking
     * @throws BusinessException {@code UNAUTHORIZED} sem restaurante; período invertido ou acima de 366 dias (400)
     */
    Page<RankingItemResponseDTO> findRanking(TipoRanking tipo, LocalDate inicio, LocalDate fim, UUID categoriaId,
                                             OrdemRanking ordem, Pageable pageable, UsuarioUserDetailsDTO usuarioLogado);
}
