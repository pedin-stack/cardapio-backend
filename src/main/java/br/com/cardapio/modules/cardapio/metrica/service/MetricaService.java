package br.com.cardapio.modules.cardapio.metrica.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.cardapio.curtida.repository.projection.ItemContagemProjection;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;
import br.com.cardapio.modules.cardapio.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.cardapio.visualizacao.repository.IVisualizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Métricas do cardápio. Curtida = pedido (decisão do produto): horários e dias "mais pedidos" vêm das curtidas
 * que ficaram; "quase pedidos" são curtidas desfeitas.
 */
@Service
@RequiredArgsConstructor
public class MetricaService implements IMetricaService {

    private static final int PERIODO_PADRAO_DIAS = 30;
    private static final int PERIODO_MAXIMO_DIAS = 366;

    private final ICurtidaRepository curtidaRepository;
    private final IVisualizacaoRepository visualizacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public MetricasResponseDTO findMetricas(final LocalDate inicio, final LocalDate fim, final UUID itemId,
                                            final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final LocalDate ate = fim != null ? fim : LocalDate.now();
        final LocalDate de = inicio != null ? inicio : ate.minusDays(PERIODO_PADRAO_DIAS - 1L);
        if (de.isAfter(ate) || ChronoUnit.DAYS.between(de, ate) >= PERIODO_MAXIMO_DIAS) {
            throw new BusinessException("Período inválido: o início deve ser anterior ao fim e o intervalo de no máximo 366 dias.");
        }
        // Intervalo semiaberto [de 00:00, ate+1 00:00): inclui o dia final inteiro.
        final LocalDateTime desde = de.atStartOfDay();
        final LocalDateTime antes = ate.plusDays(1).atStartOfDay();

        final List<MetricasResponseDTO.TempoItem> tempoPorItem = visualizacaoRepository
                .findTempoByItem(restauranteId, desde, antes).stream()
                .map(p -> new MetricasResponseDTO.TempoItem(p.getItemId(), p.getNome(), p.getVisualizacoes(),
                        p.getTempoTotalMs(), p.getTempoMedioMs()))
                .toList();

        final MetricasResponseDTO.Resumo resumo = new MetricasResponseDTO.Resumo(
                tempoPorItem.stream().mapToLong(MetricasResponseDTO.TempoItem::visualizacoes).sum(),
                tempoPorItem.stream().mapToLong(MetricasResponseDTO.TempoItem::tempoTotalMs).sum(),
                curtidaRepository.countCurtidasAtivas(restauranteId, desde, antes),
                curtidaRepository.countQuasePedidos(restauranteId, desde, antes));

        return new MetricasResponseDTO(
                de,
                ate,
                resumo,
                tempoPorItem,
                mapToContagemItens(curtidaRepository.findTopItensByCurtidas(restauranteId, desde, antes, false)),
                mapToContagemItens(curtidaRepository.findTopItensByCurtidas(restauranteId, desde, antes, true)),
                curtidaRepository.findCurtidasByHorario(restauranteId, desde, antes, itemId != null ? itemId.toString() : null)
                        .stream()
                        .map(p -> new MetricasResponseDTO.ContagemHorario(p.getDiaSemana(), p.getHora(), p.getTotal()))
                        .toList());
    }

    private static List<MetricasResponseDTO.ContagemItem> mapToContagemItens(final List<ItemContagemProjection> projecoes) {
        return projecoes.stream()
                .map(p -> new MetricasResponseDTO.ContagemItem(p.getItemId(), p.getNome(), p.getTotal()))
                .toList();
    }
}
