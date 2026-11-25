package br.com.cardapio.modules.cardapio.publico.service;

import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.adicional.dto.response.AdicionalResponseDTO;
import br.com.cardapio.modules.cardapio.adicional.model.Adicional;
import br.com.cardapio.modules.cardapio.adicional.repository.IAdicionalRepository;
import br.com.cardapio.modules.cardapio.avaliacao.repository.IAvaliacaoRepository;
import br.com.cardapio.modules.cardapio.avaliacao.repository.projection.ItemAvaliacaoProjection;
import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import br.com.cardapio.modules.cardapio.categoria.repository.ICategoriaRepository;
import br.com.cardapio.modules.cardapio.categoria.service.CategoriaService;
import br.com.cardapio.modules.cardapio.comentario.repository.IComentarioRepository;
import br.com.cardapio.modules.cardapio.comentario.repository.projection.ItemComentariosProjection;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.core.video.enums.StatusVideo;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.cardapio.publico.dto.response.CardapioPublicoResponseDTO;
import br.com.cardapio.modules.cardapio.publico.dto.response.ItemPublicoResponseDTO;
import br.com.cardapio.modules.cardapio.publico.dto.response.RestaurantePublicoResponseDTO;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Monta o cardápio público de um restaurante ativo.
 */
@Service
@RequiredArgsConstructor
public class CardapioPublicoService implements ICardapioPublicoService {

    /** Quantos itens o "Mais pedidos" mostra. */
    public static final int MAIS_PEDIDOS_LIMITE = 7;

    private final IRestauranteRepository restauranteRepository;
    private final ICurtidaRepository curtidaRepository;
    private final ICategoriaRepository categoriaRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IAdicionalRepository adicionalRepository;
    private final IAvaliacaoRepository avaliacaoRepository;
    private final IComentarioRepository comentarioRepository;
    private final IArmazenamentoArquivoService armazenamentoArquivoService;
    private final IArmazenamentoVideoService armazenamentoVideoService;

    @Override
    @Transactional(readOnly = true)
    public CardapioPublicoResponseDTO findCardapioBySlug(final String slug) {
        final Restaurante restaurante = restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));

        final List<ItemCardapio> ativos = itemCardapioRepository.findByRestauranteIdAndAtivoTrueOrderByOrdemAscNomeAsc(restaurante.getId());
        // Bucket privado: logos, capa e todas as fotos numa só assinatura (cacheada no Redis por 12 h).
        final List<String> caminhos = new ArrayList<>();
        caminhos.add(restaurante.getLogoPath());
        caminhos.add(restaurante.getCapaPath());
        caminhos.add(restaurante.getLogoCabecalhoPath());
        ativos.forEach(item -> caminhos.addAll(item.getImagens()));
        final Map<String, String> urls = armazenamentoArquivoService.findUrlsAssinadas(caminhos);

        // Adicionais, estrelas e comentários: uma consulta cada para o restaurante inteiro, indexadas por item.
        final Map<UUID, List<AdicionalResponseDTO>> adicionais = findAdicionaisPorItem(restaurante.getId());
        final Map<UUID, ItemAvaliacaoProjection> avaliacoes = avaliacaoRepository.findMediasByRestauranteId(restaurante.getId()).stream()
                .collect(Collectors.toMap(ItemAvaliacaoProjection::getItemId, Function.identity()));
        final Map<UUID, Long> comentarios = comentarioRepository.findTotaisByRestauranteId(restaurante.getId()).stream()
                .collect(Collectors.toMap(ItemComentariosProjection::getItemId, ItemComentariosProjection::getTotal));

        final List<ItemPublicoResponseDTO> itens = ativos.stream()
                .map(item -> mapToItemPublicoResponseDTO(item, urls, adicionais.getOrDefault(item.getId(), List.of()),
                        avaliacoes.get(item.getId()), comentarios.getOrDefault(item.getId(), 0L)))
                .toList();

        // Chip de categoria vazia levaria a um feed vazio: só entram categorias com algum item ativo.
        final Set<UUID> categoriasComItens = itens.stream()
                .flatMap(item -> item.categoriaIds().stream())
                .collect(Collectors.toSet());
        final List<Categoria> categorias = categoriaRepository.findByRestauranteIdOrderByOrdemAscNomeAsc(restaurante.getId());
        final boolean videoPronto = restaurante.getVideoStatus() == StatusVideo.PRONTO;

        return new CardapioPublicoResponseDTO(
                new RestaurantePublicoResponseDTO(
                        restaurante.getId(),
                        restaurante.getNome(),
                        restaurante.getSlug(),
                        restaurante.getDescricao(),
                        restaurante.getLogoPath() != null ? urls.get(restaurante.getLogoPath()) : null,
                        restaurante.getCapaPath() != null ? urls.get(restaurante.getCapaPath()) : null,
                        restaurante.getCorDestaque(),
                        restaurante.getLogoCabecalhoPath() != null ? urls.get(restaurante.getLogoCabecalhoPath()) : null,
                        videoPronto ? armazenamentoVideoService.findUrlPublica(restaurante.getVideoHlsPath()) : null,
                        videoPronto ? armazenamentoVideoService.findUrlPublica(restaurante.getVideoPosterPath()) : null,
                        restaurante.getWhatsapp(),
                        restaurante.getInstagram()),
                categorias.stream()
                        .filter(categoria -> categoriasComItens.contains(categoria.getId()))
                        .map(CategoriaService::mapToCategoriaResponseDTO)
                        .toList(),
                itens,
                curtidaRepository.findItensMaisPedidos(restaurante.getId(), MAIS_PEDIDOS_LIMITE));
    }

    /** Item → adicionais dele, em ordem alfabética do português ("Água" antes de "Bacon"). */
    private Map<UUID, List<AdicionalResponseDTO>> findAdicionaisPorItem(final UUID restauranteId) {
        final Map<UUID, List<AdicionalResponseDTO>> porItem = new HashMap<>();
        for (final Adicional adicional : adicionalRepository.findByRestauranteId(restauranteId)) {
            final AdicionalResponseDTO dto = new AdicionalResponseDTO(adicional.getId(), adicional.getNome(), adicional.getValor(), null);
            adicional.getItens().forEach(item -> porItem.computeIfAbsent(item.getId(), id -> new ArrayList<>()).add(dto));
        }
        final Collator alfabetica = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        porItem.values().forEach(lista -> lista.sort(Comparator.comparing(AdicionalResponseDTO::nome, alfabetica)));
        return porItem;
    }

    private ItemPublicoResponseDTO mapToItemPublicoResponseDTO(final ItemCardapio item, final Map<String, String> urls,
                                                               final List<AdicionalResponseDTO> adicionais,
                                                               final ItemAvaliacaoProjection avaliacao,
                                                               final long comentarios) {
        final boolean videoPronto = item.getVideoStatus() == StatusVideo.PRONTO;
        return new ItemPublicoResponseDTO(
                item.getId(),
                item.getNome(),
                item.getDescricao(),
                item.getValor(),
                item.getCategorias().stream().map(Categoria::getId).toList(),
                List.copyOf(item.getIngredientes()),
                List.copyOf(item.getEspecificacoes()),
                // Foto que falhou ao assinar fica de fora (o carrossel mostra as outras).
                item.getImagens().stream().map(urls::get).filter(Objects::nonNull).toList(),
                videoPronto ? armazenamentoVideoService.findUrlPublica(item.getVideoHlsPath()) : null,
                videoPronto ? armazenamentoVideoService.findUrlPublica(item.getVideoPosterPath()) : null,
                adicionais,
                avaliacao != null ? Math.round(avaliacao.getMedia() * 10) / 10.0 : null,
                avaliacao != null ? avaliacao.getTotal() : 0,
                comentarios);
    }
}
