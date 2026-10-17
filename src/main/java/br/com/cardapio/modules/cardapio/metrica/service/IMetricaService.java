package br.com.cardapio.modules.cardapio.metrica.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato das métricas do restaurante do gestor logado.
 */
public interface IMetricaService {

    /**
     * Agrega tempo de tela, curtidas, quase pedidos e o mapa de calor de horários no período.
     *
     * @param inicio        início (padrão: 30 dias atrás)
     * @param fim           fim, inclusive (padrão: hoje)
     * @param itemId        filtra o mapa de calor por um item (opcional)
     * @param usuarioLogado gestor (define o restaurante)
     * @return métricas do período
     * @throws BusinessException {@code UNAUTHORIZED} sem restaurante; período invertido ou acima de 366 dias (400)
     */
    MetricasResponseDTO findMetricas(LocalDate inicio, LocalDate fim, UUID itemId, UsuarioUserDetailsDTO usuarioLogado);
}
