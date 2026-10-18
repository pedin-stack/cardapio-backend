package br.com.cardapio.modules.cardapio.itemcardapio.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.infrastructure.armazenamento.SupabaseArmazenamentoService;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import br.com.cardapio.modules.cardapio.categoria.repository.ICategoriaRepository;
import br.com.cardapio.modules.cardapio.categoria.service.CategoriaService;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.request.ItemCardapioRequestDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.response.ItemCardapioResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.response.MidiaResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.response.VideoResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.enums.StatusVideo;
import br.com.cardapio.modules.cardapio.itemcardapio.event.VideoItemDescartadoEvent;
import br.com.cardapio.modules.cardapio.itemcardapio.event.VideoItemEnviadoEvent;
import br.com.cardapio.modules.cardapio.itemcardapio.model.Especificacao;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.cardapio.shared.util.RestauranteLogadoUtil;
import br.com.cardapio.modules.core.midia.service.IMidiaService;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD de itens do cardápio, restrito ao restaurante do gestor logado.
 */
@Service
@RequiredArgsConstructor
public class ItemCardapioService implements IItemCardapioService {

    private final IItemCardapioRepository itemCardapioRepository;
    private final ICategoriaRepository categoriaRepository;
    private final IRestauranteRepository restauranteRepository;
    private final IArmazenamentoArquivoService armazenamentoArquivoService;
    private final IArmazenamentoVideoService armazenamentoVideoService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ItemCardapioResponseDTO> findAllItensCardapio(final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final List<ItemCardapio> itens = itemCardapioRepository.findByRestauranteIdOrderByOrdemAscNomeAsc(restauranteId);
        // Fotos de todos os itens numa só assinatura (bucket privado).
        final Map<String, String> urls = armazenamentoArquivoService.findUrlsAssinadas(
                itens.stream().flatMap(item -> item.getImagens().stream()).toList());
        return itens.stream().map(item -> mapToItemCardapioResponseDTO(item, urls)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemCardapioResponseDTO findItemCardapioById(final UUID id, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        return mapToItemCardapioResponseDTO(findItemCardapioByIdOrThrow(id, restauranteId));
    }

    @Override
    @Transactional
    public ItemCardapioResponseDTO saveItemCardapio(final ItemCardapioRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final ItemCardapio item = new ItemCardapio();
        item.setRestaurante(restauranteRepository.getReferenceById(restauranteId));
        applyItemCardapioRequestDTO(item, dto, restauranteId);
        // O id só existe depois do save; o evento precisa dele.
        final ItemCardapio salvo = itemCardapioRepository.save(item);
        updateVideo(salvo, dto.videoCaminho());
        return mapToItemCardapioResponseDTO(salvo);
    }

    @Override
    @Transactional
    public ItemCardapioResponseDTO updateItemCardapio(final UUID id, final ItemCardapioRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final ItemCardapio item = findItemCardapioByIdOrThrow(id, restauranteId);
        applyItemCardapioRequestDTO(item, dto, restauranteId);
        updateVideo(item, dto.videoCaminho());
        return mapToItemCardapioResponseDTO(item);
    }

    @Override
    @Transactional
    public void deleteItemCardapioById(final UUID id, final UsuarioUserDetailsDTO usuarioLogado) {
        final UUID restauranteId = RestauranteLogadoUtil.findRestauranteIdOrThrow(usuarioLogado);
        final ItemCardapio item = findItemCardapioByIdOrThrow(id, restauranteId);
        if (item.getVideoHlsPath() != null) {
            eventPublisher.publishEvent(new VideoItemDescartadoEvent(item.getVideoHlsPath()));
        }
        itemCardapioRepository.delete(item);
    }

    // ---- Helpers Privados ----

    private ItemCardapio findItemCardapioByIdOrThrow(final UUID id, final UUID restauranteId) {
        return itemCardapioRepository.findByIdAndRestauranteId(id, restauranteId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage()));
    }

    private void applyItemCardapioRequestDTO(final ItemCardapio item, final ItemCardapioRequestDTO dto, final UUID restauranteId) {
        final String pasta = IMidiaService.findPastaRestaurante(restauranteId);
        final List<String> imagens = dto.imagens() == null ? List.of() : dto.imagens();
        // Redundante com o @Size do DTO de propósito: a regra de negócio não pode depender só da validação do controller.
        if (imagens.size() > ItemCardapio.MAXIMO_IMAGENS) {
            throw new BusinessException("Cada item pode ter no máximo 8 imagens.");
        }
        imagens.forEach(caminho -> SupabaseArmazenamentoService.validateCaminhoOrThrow(caminho, pasta));
        if (dto.videoCaminho() != null && !dto.videoCaminho().isBlank()) {
            SupabaseArmazenamentoService.validateCaminhoOrThrow(dto.videoCaminho(), pasta);
        }

        final List<UUID> categoriaIds = dto.categoriaIds() == null ? List.of() : dto.categoriaIds();
        final List<Categoria> categorias = categoriaIds.isEmpty()
                ? List.of()
                : categoriaRepository.findByIdInAndRestauranteId(new HashSet<>(categoriaIds), restauranteId);
        if (categorias.size() != new HashSet<>(categoriaIds).size()) {
            throw new BusinessException(BusinessExceptionMessage.CATEGORIA_INVALIDA.getMessage());
        }

        item.setNome(dto.nome().trim());
        item.setDescricao(dto.descricao());
        item.setValor(dto.valor());
        item.setAtivo(!Boolean.FALSE.equals(dto.ativo()));
        item.setOrdem(dto.ordem() != null ? dto.ordem() : 0);
        item.setCategorias(new HashSet<>(categorias));
        item.getIngredientes().clear();
        if (dto.ingredientes() != null) {
            dto.ingredientes().forEach(ingrediente -> item.getIngredientes().add(ingrediente.trim()));
        }
        item.setEspecificacoes(dto.especificacoes() == null ? new ArrayList<>() : dto.especificacoes().stream()
                .map(especificacao -> new Especificacao(
                        especificacao.nome().trim(),
                        especificacao.opcoes().stream().map(String::trim).toList()))
                .collect(Collectors.toCollection(ArrayList::new)));
        item.getImagens().clear();
        item.getImagens().addAll(imagens);
    }

    /**
     * Vídeo novo → PROCESSANDO + evento (transcodifica após o commit); vídeo removido → SEM_VIDEO.
     * Mesmo vídeo de antes: nada muda (salvar o formulário não reprocessa).
     */
    private void updateVideo(final ItemCardapio item, final String videoCaminho) {
        final String novo = videoCaminho == null || videoCaminho.isBlank() ? null : videoCaminho;
        if (Objects.equals(novo, item.getVideoOriginalPath())) {
            return;
        }
        if (item.getVideoHlsPath() != null) {
            eventPublisher.publishEvent(new VideoItemDescartadoEvent(item.getVideoHlsPath()));
        }
        item.setVideoOriginalPath(novo);
        item.setVideoHlsPath(null);
        item.setVideoPosterPath(null);
        item.setVideoStatus(novo == null ? StatusVideo.SEM_VIDEO : StatusVideo.PROCESSANDO);
        if (novo != null) {
            eventPublisher.publishEvent(new VideoItemEnviadoEvent(item.getId(), novo));
        }
    }

    // ---- Mappers Privados ----

    private ItemCardapioResponseDTO mapToItemCardapioResponseDTO(final ItemCardapio item) {
        return mapToItemCardapioResponseDTO(item, armazenamentoArquivoService.findUrlsAssinadas(item.getImagens()));
    }

    /** {@code urls}: caminho → URL assinada das fotos (bucket privado), já resolvidas em lote. */
    private ItemCardapioResponseDTO mapToItemCardapioResponseDTO(final ItemCardapio item, final Map<String, String> urls) {
        return new ItemCardapioResponseDTO(
                item.getId(),
                item.getNome(),
                item.getDescricao(),
                item.getValor(),
                item.isAtivo(),
                item.getOrdem(),
                item.getCategorias().stream()
                        .sorted(Comparator.comparingInt(Categoria::getOrdem).thenComparing(Categoria::getNome))
                        .map(CategoriaService::mapToCategoriaResponseDTO)
                        .toList(),
                List.copyOf(item.getIngredientes()),
                List.copyOf(item.getEspecificacoes()),
                item.getImagens().stream()
                        .map(caminho -> new MidiaResponseDTO(caminho, urls.get(caminho)))
                        .toList(),
                new VideoResponseDTO(
                        item.getVideoStatus(),
                        item.getVideoOriginalPath(),
                        armazenamentoVideoService.findUrlPublica(item.getVideoHlsPath()),
                        armazenamentoVideoService.findUrlPublica(item.getVideoPosterPath())),
                item.getAtualizadoEm());
    }
}
