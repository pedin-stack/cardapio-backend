package br.com.cardapio.modules.cardapio.itemcardapio.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.categoria.repository.ICategoriaRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.request.ItemCardapioRequestDTO;
import br.com.cardapio.modules.core.video.enums.StatusVideo;
import br.com.cardapio.modules.core.video.event.VideoItemDescartadoEvent;
import br.com.cardapio.modules.cardapio.itemcardapio.event.VideoItemEnviadoEvent;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import br.com.cardapio.modules.core.usuario.enums.StatusUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCardapioServiceTest {

    private static final UUID RESTAURANTE_ID = UUID.randomUUID();
    private static final String PASTA = "restaurantes/" + RESTAURANTE_ID + "/";

    @Mock
    private IItemCardapioRepository itemCardapioRepository;
    @Mock
    private ICategoriaRepository categoriaRepository;
    @Mock
    private IRestauranteRepository restauranteRepository;
    @Mock
    private IArmazenamentoArquivoService armazenamentoArquivoService;
    @Mock
    private IArmazenamentoVideoService armazenamentoVideoService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ItemCardapioService itemCardapioService;

    private final UsuarioUserDetailsDTO gestor = new UsuarioUserDetailsDTO(UUID.randomUUID(), "Gestor", "g@x.com", "hash",
            StatusUsuario.ATIVO, "Gestor Restaurante", RESTAURANTE_ID, true, Set.of());

    private ItemCardapioRequestDTO dto(final List<String> imagens, final List<UUID> categorias, final String video) {
        return new ItemCardapioRequestDTO("Cappuccino", "desc", new BigDecimal("14.90"), true, 0,
                categorias, List.of("leite"), List.of(), imagens, video);
    }

    @Test
    @DisplayName("Imagem fora da pasta do restaurante é recusada (isolamento entre restaurantes)")
    void saveItemCardapio_ImagemDeOutroRestaurante_LancaCaminhoInvalido() {
        final ItemCardapioRequestDTO dto = dto(List.of("restaurantes/" + UUID.randomUUID() + "/imagens/a.webp"), List.of(), null);

        assertThatThrownBy(() -> itemCardapioService.saveItemCardapio(dto, gestor))
                .isInstanceOf(BusinessException.class)
                .hasMessage(BusinessExceptionMessage.CAMINHO_ARQUIVO_INVALIDO.getMessage());
    }

    @Test
    @DisplayName("Mais de 8 imagens é recusado mesmo sem a validação do controller")
    void saveItemCardapio_NoveImagens_LancaExcecao() {
        final List<String> nove = IntStream.range(0, 9).mapToObj(i -> PASTA + "imagens/" + i + ".webp").toList();

        assertThatThrownBy(() -> itemCardapioService.saveItemCardapio(dto(nove, List.of(), null), gestor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("8 imagens");
    }

    @Test
    @DisplayName("Categoria de outro restaurante é recusada")
    void saveItemCardapio_CategoriaDeOutroRestaurante_LancaCategoriaInvalida() {
        final UUID categoriaAlheia = UUID.randomUUID();
        when(categoriaRepository.findByIdInAndRestauranteId(Set.of(categoriaAlheia), RESTAURANTE_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> itemCardapioService.saveItemCardapio(dto(List.of(), List.of(categoriaAlheia), null), gestor))
                .isInstanceOf(BusinessException.class)
                .hasMessage(BusinessExceptionMessage.CATEGORIA_INVALIDA.getMessage());
    }

    @Test
    @DisplayName("Item com vídeo novo nasce PROCESSANDO e publica o evento de transcodificação")
    void saveItemCardapio_ComVideo_PublicaEventoEMarcaProcessando() {
        when(itemCardapioRepository.save(any(ItemCardapio.class))).thenAnswer(inv -> inv.getArgument(0));
        final String video = PASTA + "videos/v.mp4";

        itemCardapioService.saveItemCardapio(dto(List.of(PASTA + "imagens/a.webp"), List.of(), video), gestor);

        verify(eventPublisher).publishEvent(new VideoItemEnviadoEvent(null, video));
    }

    @Test
    @DisplayName("Salvar o formulário com o mesmo vídeo não reprocessa")
    void updateItemCardapio_MesmoVideo_NaoPublicaEvento() {
        final String video = PASTA + "videos/v.mp4";
        final ItemCardapio existente = new ItemCardapio();
        existente.setVideoOriginalPath(video);
        existente.setVideoStatus(StatusVideo.PRONTO);
        final UUID id = UUID.randomUUID();
        when(itemCardapioRepository.findByIdAndRestauranteId(id, RESTAURANTE_ID)).thenReturn(Optional.of(existente));

        itemCardapioService.updateItemCardapio(id, dto(List.of(), List.of(), video), gestor);

        verify(eventPublisher, never()).publishEvent(any());
        assertThat(existente.getVideoStatus()).isEqualTo(StatusVideo.PRONTO);
    }

    @Test
    @DisplayName("Remover o vídeo volta o item para SEM_VIDEO")
    void updateItemCardapio_SemVideo_LimpaVideo() {
        final ItemCardapio existente = new ItemCardapio();
        existente.setVideoOriginalPath(PASTA + "videos/v.mp4");
        existente.setVideoHlsPath(PASTA + "videos/hls/x/master.m3u8");
        existente.setVideoStatus(StatusVideo.PRONTO);
        final UUID id = UUID.randomUUID();
        when(itemCardapioRepository.findByIdAndRestauranteId(id, RESTAURANTE_ID)).thenReturn(Optional.of(existente));

        itemCardapioService.updateItemCardapio(id, dto(List.of(), List.of(), null), gestor);

        assertThat(existente.getVideoStatus()).isEqualTo(StatusVideo.SEM_VIDEO);
        assertThat(existente.getVideoHlsPath()).isNull();
        // O HLS antigo sai do disco (depois do commit, pelo listener).
        verify(eventPublisher).publishEvent(new VideoItemDescartadoEvent(PASTA + "videos/hls/x/master.m3u8"));
    }

    @Test
    @DisplayName("Super admin (sem restaurante) não acessa itens")
    void findAllItensCardapio_SemRestaurante_LancaUnauthorized() {
        final UsuarioUserDetailsDTO superAdmin = new UsuarioUserDetailsDTO(UUID.randomUUID(), "Admin", "a@x.com", "hash",
                StatusUsuario.ATIVO, "Super Administrador", null, false, Set.of());

        assertThatThrownBy(() -> itemCardapioService.findAllItensCardapio(superAdmin))
                .isInstanceOf(BusinessException.class)
                .hasMessage(BusinessExceptionMessage.UNAUTHORIZED.getMessage());
    }
}
