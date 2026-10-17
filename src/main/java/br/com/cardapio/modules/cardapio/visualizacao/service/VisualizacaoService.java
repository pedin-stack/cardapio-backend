package br.com.cardapio.modules.cardapio.visualizacao.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.cardapio.visualizacao.dto.request.VisualizacoesRequestDTO;
import br.com.cardapio.modules.cardapio.visualizacao.model.Visualizacao;
import br.com.cardapio.modules.cardapio.visualizacao.repository.IVisualizacaoRepository;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Registro de tempo de tela (métrica "tempo gasto em cada item").
 */
@Service
@RequiredArgsConstructor
public class VisualizacaoService implements IVisualizacaoService {

    static final long DURACAO_MINIMA_MS = 500;
    static final long DURACAO_MAXIMA_MS = 10 * 60 * 1000;

    private final IVisualizacaoRepository visualizacaoRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IRestauranteRepository restauranteRepository;

    @Override
    @Transactional
    public void saveVisualizacoes(final String slug, final VisualizacoesRequestDTO dto, final UUID dispositivoId) {
        final Restaurante restaurante = restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));

        final List<VisualizacoesRequestDTO.VisualizacaoItem> validas = dto.itens().stream()
                .filter(item -> item.duracaoMs() >= DURACAO_MINIMA_MS)
                .toList();
        if (validas.isEmpty()) {
            return;
        }
        final Set<UUID> itensDoRestaurante = itemCardapioRepository.findIdsByRestauranteIdAndIdIn(
                restaurante.getId(),
                validas.stream().map(VisualizacoesRequestDTO.VisualizacaoItem::itemId).collect(Collectors.toSet()));

        final LocalDateTime agora = LocalDateTime.now();
        final List<Visualizacao> visualizacoes = validas.stream()
                .filter(item -> itensDoRestaurante.contains(item.itemId()))
                .map(item -> {
                    final Visualizacao visualizacao = new Visualizacao();
                    visualizacao.setRestaurante(restaurante);
                    visualizacao.setItem(itemCardapioRepository.getReferenceById(item.itemId()));
                    visualizacao.setDispositivoId(dispositivoId);
                    visualizacao.setDuracaoMs(Math.min(item.duracaoMs(), DURACAO_MAXIMA_MS));
                    visualizacao.setOcorridoEm(agora);
                    return visualizacao;
                })
                .toList();
        visualizacaoRepository.saveAll(visualizacoes);
    }
}
