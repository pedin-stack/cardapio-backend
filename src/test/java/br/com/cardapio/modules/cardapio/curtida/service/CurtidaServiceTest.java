package br.com.cardapio.modules.cardapio.curtida.service;

import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.cardapio.curtida.model.Curtida;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurtidaServiceTest {

    private static final String SLUG = "cafe-do-ze";

    @Mock
    private ICurtidaRepository curtidaRepository;
    @Mock
    private IItemCardapioRepository itemCardapioRepository;
    @Mock
    private IRestauranteRepository restauranteRepository;

    @InjectMocks
    private CurtidaService curtidaService;

    private final UUID itemId = UUID.randomUUID();
    private final UUID dispositivoId = UUID.randomUUID();

    private Restaurante restaurante() {
        final Restaurante restaurante = Mockito.spy(new Restaurante());
        Mockito.lenient().when(restaurante.getId()).thenReturn(UUID.randomUUID());
        return restaurante;
    }

    @Test
    @DisplayName("Restaurante desativado responde 'Serviço indisponível'")
    void saveCurtida_RestauranteDesativado_LancaServicoIndisponivel() {
        when(restauranteRepository.findBySlugAndAtivoTrue(SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curtidaService.saveCurtida(SLUG, itemId, dispositivoId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(BusinessExceptionMessage.SERVICO_INDISPONIVEL.getMessage());
    }

    @Test
    @DisplayName("Curtir de novo não duplica a curtida")
    void saveCurtida_JaCurtido_NaoSalva() {
        final Restaurante restaurante = restaurante();
        when(restauranteRepository.findBySlugAndAtivoTrue(SLUG)).thenReturn(Optional.of(restaurante));
        when(itemCardapioRepository.existsByIdAndRestauranteIdAndAtivoTrue(itemId, restaurante.getId())).thenReturn(true);
        when(curtidaRepository.findByItemIdAndDispositivoIdAndRemovidoEmIsNull(itemId, dispositivoId))
                .thenReturn(Optional.of(new Curtida()));

        curtidaService.saveCurtida(SLUG, itemId, dispositivoId);

        verify(curtidaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Item de outro restaurante não pode ser curtido")
    void saveCurtida_ItemDeOutroRestaurante_LancaNotFound() {
        final Restaurante restaurante = restaurante();
        when(restauranteRepository.findBySlugAndAtivoTrue(SLUG)).thenReturn(Optional.of(restaurante));
        when(itemCardapioRepository.existsByIdAndRestauranteIdAndAtivoTrue(itemId, restaurante.getId())).thenReturn(false);

        assertThatThrownBy(() -> curtidaService.saveCurtida(SLUG, itemId, dispositivoId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(BusinessExceptionMessage.NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Descurtir marca removidoEm (vira 'quase pedido') em vez de apagar")
    void deleteCurtida_CurtidaAtiva_MarcaRemovidoEm() {
        final Restaurante restaurante = restaurante();
        final Curtida curtida = new Curtida();
        curtida.setRestaurante(restaurante);
        when(restauranteRepository.findBySlugAndAtivoTrue(SLUG)).thenReturn(Optional.of(restaurante));
        when(curtidaRepository.findByItemIdAndDispositivoIdAndRemovidoEmIsNull(itemId, dispositivoId))
                .thenReturn(Optional.of(curtida));

        curtidaService.deleteCurtida(SLUG, itemId, dispositivoId);

        assertThat(curtida.getRemovidoEm()).isNotNull();
        verify(curtidaRepository, never()).delete(any());
    }
}
