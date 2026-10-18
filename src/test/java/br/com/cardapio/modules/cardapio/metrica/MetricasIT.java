package br.com.cardapio.modules.cardapio.metrica;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.curtida.model.Curtida;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.model.Especificacao;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.service.IMetricaService;
import br.com.cardapio.modules.cardapio.publico.dto.response.CardapioPublicoResponseDTO;
import br.com.cardapio.modules.cardapio.publico.service.ICardapioPublicoService;
import br.com.cardapio.modules.cardapio.visualizacao.model.Visualizacao;
import br.com.cardapio.modules.cardapio.visualizacao.repository.IVisualizacaoRepository;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.restaurante.repository.IRestauranteRepository;
import br.com.cardapio.modules.core.usuario.enums.StatusUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sobe o contexto contra um PostgreSQL real: aplica todas as migrations, valida o mapeamento
 * (ddl-auto=validate) e confere as consultas nativas das métricas.
 */
@SpringBootTest(properties = {
        "api.security.token.secret=segredo-de-teste",
        "app.supabase.url=http://localhost",
        "app.supabase.service-key=chave-de-teste",
        "spring.cache.type=none"
})
@Testcontainers
class MetricasIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void datasource(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private IRestauranteRepository restauranteRepository;
    @Autowired
    private IItemCardapioRepository itemCardapioRepository;
    @Autowired
    private ICurtidaRepository curtidaRepository;
    @Autowired
    private IVisualizacaoRepository visualizacaoRepository;
    @Autowired
    private IMetricaService metricaService;
    @Autowired
    private ICardapioPublicoService cardapioPublicoService;

    private Restaurante criarRestaurante(final boolean ativo) {
        final Restaurante restaurante = new Restaurante();
        restaurante.setNome("Café");
        restaurante.setSlug("cafe-" + UUID.randomUUID().toString().substring(0, 8));
        restaurante.setAtivo(ativo);
        return restauranteRepository.save(restaurante);
    }

    private ItemCardapio criarItem(final Restaurante restaurante, final String nome) {
        final ItemCardapio item = new ItemCardapio();
        item.setNome(nome);
        item.setValor(new BigDecimal("10.00"));
        item.setRestaurante(restaurante);
        return itemCardapioRepository.save(item);
    }

    private void curtir(final Restaurante restaurante, final ItemCardapio item, final LocalDateTime quando, final boolean removida) {
        final Curtida curtida = new Curtida();
        curtida.setRestaurante(restaurante);
        curtida.setItem(item);
        curtida.setDispositivoId(UUID.randomUUID());
        curtida.setCurtidoEm(quando);
        curtida.setRemovidoEm(removida ? quando.plusMinutes(1) : null);
        curtidaRepository.save(curtida);
    }

    @Test
    @DisplayName("Métricas: mais curtidos, quase pedidos, tempo por item e mapa de calor por dia/hora")
    void findMetricas_DadosNoPeriodo_AgregaCorretamente() {
        final Restaurante restaurante = criarRestaurante(true);
        final ItemCardapio cafe = criarItem(restaurante, "Café");
        final ItemCardapio bolo = criarItem(restaurante, "Bolo");
        // 2026-10-12 é segunda-feira (ISODOW 1).
        final LocalDateTime segunda10h = LocalDateTime.of(2026, 10, 12, 10, 15);
        curtir(restaurante, cafe, segunda10h, false);
        curtir(restaurante, cafe, segunda10h.plusMinutes(5), false);
        curtir(restaurante, bolo, segunda10h, true);

        final Visualizacao visualizacao = new Visualizacao();
        visualizacao.setRestaurante(restaurante);
        visualizacao.setItem(bolo);
        visualizacao.setDispositivoId(UUID.randomUUID());
        visualizacao.setDuracaoMs(4000);
        visualizacao.setOcorridoEm(segunda10h);
        visualizacaoRepository.save(visualizacao);

        final UsuarioUserDetailsDTO gestor = new UsuarioUserDetailsDTO(UUID.randomUUID(), "G", "g@x.com", "h",
                StatusUsuario.ATIVO, "Gestor Restaurante", restaurante.getId(), true, Set.of());
        final MetricasResponseDTO metricas = metricaService.findMetricas(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null, gestor);

        assertThat(metricas.resumo().curtidas()).isEqualTo(2);
        assertThat(metricas.resumo().quasePedidos()).isEqualTo(1);
        assertThat(metricas.resumo().tempoTotalMs()).isEqualTo(4000);
        assertThat(metricas.maisCurtidos()).singleElement()
                .satisfies(c -> assertThat(c.nome()).isEqualTo("Café"))
                .satisfies(c -> assertThat(c.total()).isEqualTo(2));
        assertThat(metricas.quasePedidos()).singleElement().satisfies(c -> assertThat(c.nome()).isEqualTo("Bolo"));
        assertThat(metricas.tempoPorItem()).hasSize(2);
        assertThat(metricas.tempoPorItem().getFirst().nome()).isEqualTo("Bolo");
        assertThat(metricas.curtidasPorHorario()).containsExactly(new MetricasResponseDTO.ContagemHorario(1, 10, 2));

        final MetricasResponseDTO soBolo = metricaService.findMetricas(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), bolo.getId(), gestor);
        assertThat(soBolo.curtidasPorHorario()).isEmpty();
    }

    @Test
    @DisplayName("Restaurante desativado: cardápio público responde 'Serviço indisponível'")
    void findCardapioBySlug_RestauranteDesativado_LancaServicoIndisponivel() {
        final Restaurante ativo = criarRestaurante(true);
        criarItem(ativo, "Café");
        final Restaurante desativado = criarRestaurante(false);

        assertThat(cardapioPublicoService.findCardapioBySlug(ativo.getSlug()).itens()).hasSize(1);
        assertThatThrownBy(() -> cardapioPublicoService.findCardapioBySlug(desativado.getSlug()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Serviço indisponível.");
    }

    @Test
    @DisplayName("Especificações (JSONB) e cor do restaurante voltam do banco no cardápio público")
    void findCardapioBySlug_ComEspecificacoesECor_DevolveComoGravado() {
        final Restaurante restaurante = criarRestaurante(true);
        restaurante.setCorDestaque("#e9b872");
        restauranteRepository.save(restaurante);
        final ItemCardapio item = new ItemCardapio();
        item.setNome("Picanha");
        item.setValor(new BigDecimal("98.90"));
        item.setRestaurante(restaurante);
        final Especificacao ponto = new Especificacao("Ponto da carne", List.of("Mal passado", "Ao ponto", "Bem passado"));
        item.setEspecificacoes(new ArrayList<>(List.of(ponto)));
        itemCardapioRepository.save(item);

        final CardapioPublicoResponseDTO cardapio = cardapioPublicoService.findCardapioBySlug(restaurante.getSlug());

        assertThat(cardapio.restaurante().corDestaque()).isEqualTo("#e9b872");
        assertThat(cardapio.itens().getFirst().especificacoes()).containsExactly(ponto);
    }
}
