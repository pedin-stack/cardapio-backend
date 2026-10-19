package br.com.cardapio.modules.cardapio.curtida.repository;

import br.com.cardapio.modules.cardapio.curtida.model.Curtida;
import br.com.cardapio.modules.cardapio.curtida.repository.projection.HorarioContagemProjection;
import br.com.cardapio.modules.cardapio.curtida.repository.projection.ItemContagemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de {@link Curtida} e das agregações de métricas sobre elas (SQL nativo: EXTRACT/FILTER do Postgres).
 */
@Repository
public interface ICurtidaRepository extends JpaRepository<Curtida, UUID> {

    Optional<Curtida> findByItemIdAndDispositivoIdAndRemovidoEmIsNull(UUID itemId, UUID dispositivoId);

    /**
     * "Mais pedidos" do cardápio público: itens ativos com mais curtidas que ficaram, desde sempre, do mais curtido
     * para o menos. Item sem curtida não entra (lista vazia = o chip não aparece).
     */
    @Query(value = """
            SELECT c.item_id
            FROM public.curtidas c
            JOIN public.itens_cardapio i ON i.id = c.item_id AND i.ativo
            WHERE c.restaurante_id = :restauranteId
              AND c.removido_em IS NULL
            GROUP BY c.item_id, i.ordem, i.nome
            ORDER BY COUNT(*) DESC, i.ordem, i.nome
            LIMIT :limite
            """, nativeQuery = true)
    List<UUID> findItensMaisPedidos(@Param("restauranteId") UUID restauranteId, @Param("limite") int limite);

    /**
     * Ranking paginado de itens por curtidas no período (só itens com curtida). {@code removidas = false}: curtidas
     * que ficaram ("pedidos"); {@code removidas = true}: curtidas desfeitas ("quase pedidos").
     * {@code categoriaId} (opcional) e {@code ordem} ({@code OrdemRanking}) chegam como texto, como o {@code itemId}
     * do mapa de calor. Empates seguem nome e id: a paginação não repete nem pula itens de nome igual. O nome usa a
     * collation ICU pt-BR: a padrão da imagem alpine compara bytes e mandaria "Água" para depois do "Z".
     */
    @Query(value = """
            SELECT i.id AS "itemId", i.nome AS "nome", COUNT(c.id) AS "total"
            FROM public.curtidas c
            JOIN public.itens_cardapio i ON i.id = c.item_id
            WHERE c.restaurante_id = :restauranteId
              AND c.curtido_em >= :inicio AND c.curtido_em < :fim
              AND (c.removido_em IS NOT NULL) = :removidas
              AND (CAST(:categoriaId AS VARCHAR) IS NULL OR EXISTS (
                    SELECT 1 FROM public.itens_categorias ic
                    WHERE ic.item_id = c.item_id AND ic.categoria_id = CAST(:categoriaId AS UUID)))
            GROUP BY i.id, i.nome
            ORDER BY CASE WHEN CAST(:ordem AS VARCHAR) = 'MAIOR' THEN COUNT(c.id) END DESC,
                     CASE WHEN CAST(:ordem AS VARCHAR) = 'MENOR' THEN COUNT(c.id) END ASC,
                     i.nome COLLATE "pt-BR-x-icu", i.id
            """,
            countQuery = """
            SELECT COUNT(DISTINCT c.item_id)
            FROM public.curtidas c
            WHERE c.restaurante_id = :restauranteId
              AND c.curtido_em >= :inicio AND c.curtido_em < :fim
              AND (c.removido_em IS NOT NULL) = :removidas
              AND (CAST(:categoriaId AS VARCHAR) IS NULL OR EXISTS (
                    SELECT 1 FROM public.itens_categorias ic
                    WHERE ic.item_id = c.item_id AND ic.categoria_id = CAST(:categoriaId AS UUID)))
            """, nativeQuery = true)
    Page<ItemContagemProjection> findRankingByCurtidas(@Param("restauranteId") UUID restauranteId,
                                                       @Param("inicio") LocalDateTime inicio,
                                                       @Param("fim") LocalDateTime fim,
                                                       @Param("removidas") boolean removidas,
                                                       @Param("categoriaId") String categoriaId,
                                                       @Param("ordem") String ordem,
                                                       Pageable pageable);

    /**
     * Mapa de calor das curtidas que ficaram ("pedidos") por dia da semana × hora, opcionalmente de um item.
     * {@code itemId} chega como texto: um UUID nulo sem tipo quebraria o bind no Postgres.
     * Aliases entre aspas: sem elas o Postgres os devolve em minúsculas e a projeção não casa os getters.
     */
    @Query(value = """
            SELECT CAST(EXTRACT(ISODOW FROM c.curtido_em) AS INTEGER) AS "diaSemana",
                   CAST(EXTRACT(HOUR FROM c.curtido_em) AS INTEGER) AS "hora",
                   COUNT(*) AS "total"
            FROM public.curtidas c
            WHERE c.restaurante_id = :restauranteId
              AND c.curtido_em >= :inicio AND c.curtido_em < :fim
              AND c.removido_em IS NULL
              AND (CAST(:itemId AS VARCHAR) IS NULL OR c.item_id = CAST(:itemId AS UUID))
            GROUP BY 1, 2
            """, nativeQuery = true)
    List<HorarioContagemProjection> findCurtidasByHorario(@Param("restauranteId") UUID restauranteId,
                                                          @Param("inicio") LocalDateTime inicio,
                                                          @Param("fim") LocalDateTime fim,
                                                          @Param("itemId") String itemId);

    @Query(value = """
            SELECT COUNT(*) FILTER (WHERE c.removido_em IS NULL)
            FROM public.curtidas c
            WHERE c.restaurante_id = :restauranteId AND c.curtido_em >= :inicio AND c.curtido_em < :fim
            """, nativeQuery = true)
    long countCurtidasAtivas(@Param("restauranteId") UUID restauranteId,
                             @Param("inicio") LocalDateTime inicio,
                             @Param("fim") LocalDateTime fim);

    @Query(value = """
            SELECT COUNT(*) FILTER (WHERE c.removido_em IS NOT NULL)
            FROM public.curtidas c
            WHERE c.restaurante_id = :restauranteId AND c.curtido_em >= :inicio AND c.curtido_em < :fim
            """, nativeQuery = true)
    long countQuasePedidos(@Param("restauranteId") UUID restauranteId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim);
}
