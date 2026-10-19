package br.com.cardapio.modules.cardapio.metrica;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.cardapio.categoria.dto.response.CategoriaResponseDTO;
import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import br.com.cardapio.modules.cardapio.categoria.repository.ICategoriaRepository;
import br.com.cardapio.modules.cardapio.categoria.service.ICategoriaService;
import br.com.cardapio.modules.cardapio.curtida.model.Curtida;
import br.com.cardapio.modules.cardapio.curtida.repository.ICurtidaRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.dto.response.ItemCardapioResponseDTO;
import br.com.cardapio.modules.cardapio.itemcardapio.model.Especificacao;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import br.com.cardapio.modules.cardapio.itemcardapio.service.IItemCardapioService;
import br.com.cardapio.modules.cardapio.metrica.dto.response.MetricasResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.dto.response.RankingItemResponseDTO;
import br.com.cardapio.modules.cardapio.metrica.enums.OrdemRanking;
import br.com.cardapio.modules.cardapio.metrica.enums.TipoRanking;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
    private ICategoriaRepository categoriaRepository;
    @Autowired
    private IMetricaService metricaService;
    @Autowired
    private IItemCardapioService itemCardapioService;
    @Autowired
    private ICategoriaService categoriaService;
    @Autowired
    private ICardapioPublicoService cardapioPublicoService;

    private Restaurante criarRestaurante(final boolean ativo) {
        final Restaurante restaurante = new Restaurante();
        restaurante.setNome("Café");
        restaurante.setSlug("cafe-" + UUID.randomUUID().toString().substring(0, 8));
        restaurante.setAtivo(ativo);
        return restauranteRepository.save(restaurante);
    }

    private ItemCardapio criarItem(final Restaurante restaurante, final String nome, final Categoria... categorias) {
        final ItemCardapio item = new ItemCardapio();
        item.setNome(nome);
        item.setValor(new BigDecimal("10.00"));
        item.setRestaurante(restaurante);
        item.setCategorias(new HashSet<>(Set.of(categorias)));
        return itemCardapioRepository.save(item);
    }

    private Categoria criarCategoria(final Restaurante restaurante, final String nome) {
        final Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setRestaurante(restaurante);
        return categoriaRepository.save(categoria);
    }

    private static UsuarioUserDetailsDTO gestor(final Restaurante restaurante) {
        return new UsuarioUserDetailsDTO(UUID.randomUUID(), "G", "g@x.com", "h",
                StatusUsuario.ATIVO, "Gestor Restaurante", restaurante.getId(), true, Set.of());
    }

    private static List<String> nomes(final Page<RankingItemResponseDTO> pagina) {
        return pagina.getContent().stream().map(RankingItemResponseDTO::nome).toList();
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

        final UsuarioUserDetailsDTO gestor = gestor(restaurante);
        final LocalDate de = LocalDate.of(2026, 10, 1);
        final LocalDate ate = LocalDate.of(2026, 10, 31);
        final MetricasResponseDTO metricas = metricaService.findMetricas(de, ate, null, gestor);

        assertThat(metricas.resumo().curtidas()).isEqualTo(2);
        assertThat(metricas.resumo().quasePedidos()).isEqualTo(1);
        assertThat(metricas.resumo().visualizacoes()).isEqualTo(1);
        assertThat(metricas.resumo().tempoTotalMs()).isEqualTo(4000);
        assertThat(metricas.curtidasPorHorario()).containsExactly(new MetricasResponseDTO.ContagemHorario(1, 10, 2));

        final PageRequest pagina = PageRequest.of(0, 10);
        assertThat(metricaService.findRanking(TipoRanking.CURTIDOS, de, ate, null, OrdemRanking.MAIOR, pagina, gestor).getContent())
                .singleElement()
                .satisfies(r -> assertThat(r.nome()).isEqualTo("Café"))
                .satisfies(r -> assertThat(r.valor()).isEqualTo(2));
        assertThat(metricaService.findRanking(TipoRanking.QUASE_PEDIDOS, de, ate, null, OrdemRanking.MAIOR, pagina, gestor).getContent())
                .singleElement().satisfies(r -> assertThat(r.nome()).isEqualTo("Bolo"));
        // Tempo: todos os itens, inclusive o sem visualização.
        final Page<RankingItemResponseDTO> tempo = metricaService.findRanking(TipoRanking.TEMPO, de, ate, null, OrdemRanking.MAIOR, pagina, gestor);
        assertThat(nomes(tempo)).containsExactly("Bolo", "Café");
        assertThat(tempo.getContent().getFirst())
                .satisfies(r -> assertThat(r.valor()).isEqualTo(4000))
                .satisfies(r -> assertThat(r.visualizacoes()).isEqualTo(1));

        final MetricasResponseDTO soBolo = metricaService.findMetricas(de, ate, bolo.getId(), gestor);
        assertThat(soBolo.curtidasPorHorario()).isEmpty();
    }

    @Test
    @DisplayName("Ranking: filtra por categoria, ordena por maior, menor e nome (pt-BR) e pagina sem repetir")
    void findRanking_CategoriaOrdemEPagina_DevolveFatiaCerta() {
        final Restaurante restaurante = criarRestaurante(true);
        final Categoria bebidas = criarCategoria(restaurante, "Bebidas");
        final ItemCardapio suco = criarItem(restaurante, "Suco", bebidas);
        final ItemCardapio cha = criarItem(restaurante, "Chá", bebidas);
        final ItemCardapio agua = criarItem(restaurante, "Água", bebidas);
        final ItemCardapio bolo = criarItem(restaurante, "Bolo");
        final LocalDateTime quando = LocalDateTime.of(2026, 10, 12, 10, 0);
        for (int i = 0; i < 3; i++) curtir(restaurante, suco, quando, false);
        for (int i = 0; i < 2; i++) curtir(restaurante, cha, quando, false);
        curtir(restaurante, agua, quando, false);
        // Fora da categoria: não entra mesmo sendo o mais curtido.
        for (int i = 0; i < 9; i++) curtir(restaurante, bolo, quando, false);

        final UsuarioUserDetailsDTO gestor = gestor(restaurante);
        final LocalDate de = LocalDate.of(2026, 10, 1);
        final LocalDate ate = LocalDate.of(2026, 10, 31);

        final Page<RankingItemResponseDTO> primeira = metricaService.findRanking(
                TipoRanking.CURTIDOS, de, ate, bebidas.getId(), OrdemRanking.MAIOR, PageRequest.of(0, 2), gestor);
        assertThat(primeira.getTotalElements()).isEqualTo(3);
        assertThat(nomes(primeira)).containsExactly("Suco", "Chá");
        assertThat(nomes(metricaService.findRanking(
                TipoRanking.CURTIDOS, de, ate, bebidas.getId(), OrdemRanking.MAIOR, PageRequest.of(1, 2), gestor)))
                .containsExactly("Água");
        assertThat(nomes(metricaService.findRanking(
                TipoRanking.CURTIDOS, de, ate, bebidas.getId(), OrdemRanking.MENOR, PageRequest.of(0, 10), gestor)))
                .containsExactly("Água", "Chá", "Suco");
        // "Água" antes de "Bolo": a ordem alfabética ignora o acento.
        assertThat(nomes(metricaService.findRanking(
                TipoRanking.TEMPO, de, ate, null, OrdemRanking.NOME, PageRequest.of(0, 10), gestor)))
                .containsExactly("Água", "Bolo", "Chá", "Suco");
        assertThat(metricaService.findRanking(
                TipoRanking.TEMPO, de, ate, bebidas.getId(), OrdemRanking.NOME, PageRequest.of(0, 10), gestor).getTotalElements())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Itens e categorias paginados: filtro por várias categorias sem repetir o item que está em duas")
    void findItensCardapioPagina_FiltroPorCategorias_PaginaSemRepetir() {
        final Restaurante restaurante = criarRestaurante(true);
        final Categoria bebidas = criarCategoria(restaurante, "Bebidas");
        final Categoria doces = criarCategoria(restaurante, "Doces");
        criarItem(restaurante, "Milkshake", bebidas, doces);
        criarItem(restaurante, "Suco", bebidas);
        criarItem(restaurante, "Pudim", doces);
        criarItem(restaurante, "Picanha");
        final UsuarioUserDetailsDTO gestor = gestor(restaurante);
        final Sort ordem = Sort.by("ordem", "nome", "id");

        final Page<ItemCardapioResponseDTO> filtrados = itemCardapioService.findItensCardapioPagina(
                List.of(bebidas.getId(), doces.getId()), PageRequest.of(0, 2, ordem), gestor);
        assertThat(filtrados.getTotalElements()).isEqualTo(3);
        assertThat(filtrados.getContent()).extracting(ItemCardapioResponseDTO::nome).containsExactly("Milkshake", "Pudim");
        assertThat(itemCardapioService.findItensCardapioPagina(List.of(), PageRequest.of(0, 21, ordem), gestor).getTotalElements())
                .isEqualTo(4);
        assertThat(categoriaService.findCategoriasPagina(PageRequest.of(1, 1, ordem), gestor).getContent())
                .extracting(CategoriaResponseDTO::nome).containsExactly("Doces");
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
    @DisplayName("Mais pedidos: só curtidas que ficaram, de itens ativos, do mais curtido para o menos, até 7")
    void findCardapioBySlug_ComCurtidas_DevolveMaisPedidosEmOrdem() {
        final Restaurante restaurante = criarRestaurante(true);
        final LocalDateTime agora = LocalDateTime.now();
        final ItemCardapio tres = criarItem(restaurante, "Três");
        final ItemCardapio um = criarItem(restaurante, "Um");
        final ItemCardapio dois = criarItem(restaurante, "Dois");
        for (int i = 0; i < 3; i++) curtir(restaurante, tres, agora, false);
        for (int i = 0; i < 2; i++) curtir(restaurante, dois, agora, false);
        curtir(restaurante, um, agora, false);
        // Removidas não contam ("quase pedidos"), nem as de item escondido do cardápio.
        for (int i = 0; i < 5; i++) curtir(restaurante, um, agora, true);
        final ItemCardapio inativo = criarItem(restaurante, "Inativo");
        inativo.setAtivo(false);
        itemCardapioRepository.save(inativo);
        for (int i = 0; i < 9; i++) curtir(restaurante, inativo, agora, false);
        // Oito itens com 1 curtida: o limite corta em 7.
        for (int i = 0; i < 8; i++) curtir(restaurante, criarItem(restaurante, "Extra " + i), agora, false);

        final List<UUID> maisPedidos = cardapioPublicoService.findCardapioBySlug(restaurante.getSlug()).maisPedidos();

        assertThat(maisPedidos).hasSize(7).startsWith(tres.getId(), dois.getId()).doesNotContain(inativo.getId());
        assertThat(cardapioPublicoService.findCardapioBySlug(criarRestaurante(true).getSlug()).maisPedidos()).isEmpty();
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
