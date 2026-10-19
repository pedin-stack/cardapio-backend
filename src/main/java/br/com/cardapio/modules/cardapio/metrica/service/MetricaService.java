package br.com.cardapio.modules.cardapio.metrica.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.dto.response.RankingItemResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.enums.OrdemRanking;
import br.com.cardapio.modules.cardapio.metrica.enums.TipoRanking;
import br.com.cardapio.modules.cardapio.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.cardapio.visualizacao.repository.IVisualizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        final Periodo periodo = resolverPeriodo(inicio, fim);
        final LocalDateTime desde = periodo.desde();
        final LocalDateTime antes = periodo.antes();

        final MetricasResponseDTO.Resumo resumo = new MetricasResponseDTO.Resumo(
                visualizacaoRepository.countVisualizacoes(restauranteId, desde, antes),
                visualizacaoRepository.sumDuracaoMs(restauranteId, desde, antes),
                curtidaRepository.countCurtidasAtivas(restauranteId, desde, antes),
                curtidaRepository.countQuasePedidos(restauranteId, desde, antes));

        return new MetricasResponseDTO(
                periodo.de(),
                periodo.ate(),
                resumo,
                curtidaRepository.findCurtidasByHorario(restauranteId, desde, antes, itemId != null ? itemId.toString() : null)
                        .stream()
                        .map(p -> new MetricasResponseDTO.ContagemHorario(p.getDiaSemana(), p.getHora(), p.getTotal()))
                        .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RankingItemResponseDTO> findRanking(final TipoRanking tipo, final LocalDate inicio, final LocalDate fim,
                                                    final UUID categoriaId, final OrdemRanking ordem, final Pageable pageable,
                                                    final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final Periodo periodo = resolverPeriodo(inicio, fim);
        final String categoria = categoriaId != null ? categoriaId.toString() : null;
        // Consulta nativa: a ordem vem de `ordem`; um sort vindo do cliente quebraria o SQL.
        final Pageable pagina = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        if (tipo == TipoRanking.TEMPO) {
            return visualizacaoRepository
                    .findRankingByTempo(restauranteId, periodo.desde(), periodo.antes(), categoria, ordem.name(), pagina)
                    .map(p -> new RankingItemResponseDTO(p.getItemId(), p.getNome(), p.getTempoMedioMs(),
                            p.getVisualizacoes(), p.getTempoTotalMs()));
        }
        return curtidaRepository
                .findRankingByCurtidas(restauranteId, periodo.desde(), periodo.antes(), tipo == TipoRanking.QUASE_PEDIDOS,
                        categoria, ordem.name(), pagina)
                .map(p -> new RankingItemResponseDTO(p.getItemId(), p.getNome(), p.getTotal(), null, null));
    }

    // ---- Helpers Privados ----

    /** Datas inclusive; consultas usam o intervalo semiaberto [de 00:00, ate+1 00:00), que inclui o dia final inteiro. */
    private record Periodo(LocalDate de, LocalDate ate) {
        LocalDateTime desde() {
            return de.atStartOfDay();
        }

        LocalDateTime antes() {
            return ate.plusDays(1).atStartOfDay();
        }
    }

    private static Periodo resolverPeriodo(final LocalDate inicio, final LocalDate fim) {
        final LocalDate ate = fim != null ? fim : LocalDate.now();
        final LocalDate de = inicio != null ? inicio : ate.minusDays(PERIODO_PADRAO_DIAS - 1L);
        if (de.isAfter(ate) || ChronoUnit.DAYS.between(de, ate) >= PERIODO_MAXIMO_DIAS) {
            throw new BusinessException("Período inválido: o início deve ser anterior ao fim e o intervalo de no máximo 366 dias.");
        }
        return new Periodo(de, ate);
    }
}
