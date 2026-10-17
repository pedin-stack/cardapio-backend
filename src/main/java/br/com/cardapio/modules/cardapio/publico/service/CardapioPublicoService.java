package br.com.cardapio.modules.cardapio.publico.service;

import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import br.com.cardapio.modules.cardapio.categoria.repository.ICategoriaRepository;
import br.com.cardapio.modules.cardapio.categoria.service.CategoriaService;
import br.com.cardapio.modules.cardapio.itemcardapio.enums.StatusVideo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Monta o cardápio público de um restaurante ativo.
 */
@Service
@RequiredArgsConstructor
public class CardapioPublicoService implements ICardapioPublicoService {

    private final IRestauranteRepository restauranteRepository;
    private final ICategoriaRepository categoriaRepository;
    private final IItemCardapioRepository itemCardapioRepository;
    private final IArmazenamentoArquivoService armazenamentoArquivoService;
    private final IArmazenamentoVideoService armazenamentoVideoService;

    @Override
    @Transactional(readOnly = true)
    public CardapioPublicoResponseDTO findCardapioBySlug(final String slug) {
        final Restaurante restaurante = restauranteRepository.findBySlugAndAtivoTrue(slug)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage()));

        final List<ItemCardapio> ativos = itemCardapioRepository.findByRestauranteIdAndAtivoTrueOrderByOrdemAscNomeAsc(restaurante.getId());
        // Bucket privado: logo, capa e todas as fotos numa só assinatura (cacheada no Redis por 12 h).
        final List<String> caminhos = new ArrayList<>();
        caminhos.add(restaurante.getLogoPath());
        caminhos.add(restaurante.getCapaPath());
        ativos.forEach(item -> caminhos.addAll(item.getImagens()));
        final Map<String, String> urls = armazenamentoArquivoService.findUrlsAssinadas(caminhos);

        final List<ItemPublicoResponseDTO> itens = ativos.stream()
                .map(item -> mapToItemPublicoResponseDTO(item, urls))
                .toList();

        // Chip de categoria vazia levaria a um feed vazio: só entram categorias com algum item ativo.
        final Set<UUID> categoriasComItens = itens.stream()
                .flatMap(item -> item.categoriaIds().stream())
                .collect(Collectors.toSet());
        final List<Categoria> categorias = categoriaRepository.findByRestauranteIdOrderByOrdemAscNomeAsc(restaurante.getId());

        return new CardapioPublicoResponseDTO(
                new RestaurantePublicoResponseDTO(
                        restaurante.getId(),
                        restaurante.getNome(),
                        restaurante.getSlug(),
                        restaurante.getDescricao(),
                        restaurante.getLogoPath() != null ? urls.get(restaurante.getLogoPath()) : null,
                        restaurante.getCapaPath() != null ? urls.get(restaurante.getCapaPath()) : null),
                categorias.stream()
                        .filter(categoria -> categoriasComItens.contains(categoria.getId()))
                        .map(CategoriaService::mapToCategoriaResponseDTO)
                        .toList(),
                itens);
    }

    private ItemPublicoResponseDTO mapToItemPublicoResponseDTO(final ItemCardapio item, final Map<String, String> urls) {
        final boolean videoPronto = item.getVideoStatus() == StatusVideo.PRONTO;
        return new ItemPublicoResponseDTO(
                item.getId(),
                item.getNome(),
                item.getDescricao(),
                item.getValor(),
                item.getCategorias().stream().map(Categoria::getId).toList(),
                List.copyOf(item.getIngredientes()),
                // Foto que falhou ao assinar fica de fora (o carrossel mostra as outras).
                item.getImagens().stream().map(urls::get).filter(Objects::nonNull).toList(),
                videoPronto ? armazenamentoVideoService.findUrlPublica(item.getVideoHlsPath()) : null,
                videoPronto ? armazenamentoVideoService.findUrlPublica(item.getVideoPosterPath()) : null);
    }
}
