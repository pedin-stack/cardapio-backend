package br.com.cardapio.modules.cardapio.curtida.repository;

import br.com.cardapio.modules.cardapio.curtida.model.Curtida;
import br.com.cardapio.modules.cardapio.curtida.repository.projection.HorarioContagemProjection;
import br.com.cardapio.modules.cardapio.curtida.repository.projection.ItemContagemProjection;
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
     * Itens por curtidas no período. {@code removidas = false}: curtidas que ficaram ("pedidos");
     * {@code removidas = true}: curtidas desfeitas ("quase pedidos").
     */
    @Query(value = """
            SELECT i.id AS "itemId", i.nome AS "nome", COUNT(c.id) AS "total"
            FROM public.curtidas c
            JOIN public.itens_cardapio i ON i.id = c.item_id
            WHERE c.restaurante_id = :restauranteId
              AND c.curtido_em >= :inicio AND c.curtido_em < :fim
              AND (c.removido_em IS NOT NULL) = :removidas
            GROUP BY i.id, i.nome
            ORDER BY "total" DESC, i.nome
            LIMIT 10
            """, nativeQuery = true)
    List<ItemContagemProjection> findTopItensByCurtidas(@Param("restauranteId") UUID restauranteId,
                                                        @Param("inicio") LocalDateTime inicio,
                                                        @Param("fim") LocalDateTime fim,
                                                        @Param("removidas") boolean removidas);

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
