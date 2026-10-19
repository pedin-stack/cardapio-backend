package br.com.cardapio.modules.cardapio.visualizacao.repository;

import br.com.cardapio.modules.cardapio.visualizacao.model.Visualizacao;
import br.com.cardapio.modules.cardapio.visualizacao.repository.projection.ItemTempoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repositório de {@link Visualizacao} e da agregação de tempo de tela por item.
 */
@Repository
public interface IVisualizacaoRepository extends JpaRepository<Visualizacao, UUID> {

    /**
     * Ranking paginado de tempo médio de tela: todos os itens do restaurante (inclusive sem visualização no período,
     * com zero). Filtro de categoria e ordem como em {@code ICurtidaRepository#findRankingByCurtidas}.
     */
    @Query(value = """
            SELECT i.id AS "itemId", i.nome AS "nome",
                   COUNT(v.id) AS "visualizacoes",
                   CAST(COALESCE(SUM(v.duracao_ms), 0) AS BIGINT) AS "tempoTotalMs",
                   CAST(COALESCE(AVG(v.duracao_ms), 0) AS BIGINT) AS "tempoMedioMs"
            FROM public.itens_cardapio i
            LEFT JOIN public.visualizacoes v
                   ON v.item_id = i.id AND v.ocorrido_em >= :inicio AND v.ocorrido_em < :fim
            WHERE i.restaurante_id = :restauranteId
              AND (CAST(:categoriaId AS VARCHAR) IS NULL OR EXISTS (
                    SELECT 1 FROM public.itens_categorias ic
                    WHERE ic.item_id = i.id AND ic.categoria_id = CAST(:categoriaId AS UUID)))
            GROUP BY i.id, i.nome
            ORDER BY CASE WHEN CAST(:ordem AS VARCHAR) = 'MAIOR' THEN COALESCE(AVG(v.duracao_ms), 0) END DESC,
                     CASE WHEN CAST(:ordem AS VARCHAR) = 'MENOR' THEN COALESCE(AVG(v.duracao_ms), 0) END ASC,
                     i.nome COLLATE "pt-BR-x-icu", i.id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM public.itens_cardapio i
            WHERE i.restaurante_id = :restauranteId
              AND (CAST(:categoriaId AS VARCHAR) IS NULL OR EXISTS (
                    SELECT 1 FROM public.itens_categorias ic
                    WHERE ic.item_id = i.id AND ic.categoria_id = CAST(:categoriaId AS UUID)))
            """, nativeQuery = true)
    Page<ItemTempoProjection> findRankingByTempo(@Param("restauranteId") UUID restauranteId,
                                                 @Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim,
                                                 @Param("categoriaId") String categoriaId,
                                                 @Param("ordem") String ordem,
                                                 Pageable pageable);

    @Query(value = """
            SELECT COUNT(*)
            FROM public.visualizacoes v
            WHERE v.restaurante_id = :restauranteId AND v.ocorrido_em >= :inicio AND v.ocorrido_em < :fim
            """, nativeQuery = true)
    long countVisualizacoes(@Param("restauranteId") UUID restauranteId,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fim") LocalDateTime fim);

    @Query(value = """
            SELECT CAST(COALESCE(SUM(v.duracao_ms), 0) AS BIGINT)
            FROM public.visualizacoes v
            WHERE v.restaurante_id = :restauranteId AND v.ocorrido_em >= :inicio AND v.ocorrido_em < :fim
            """, nativeQuery = true)
    long sumDuracaoMs(@Param("restauranteId") UUID restauranteId,
                      @Param("inicio") LocalDateTime inicio,
                      @Param("fim") LocalDateTime fim);
}
