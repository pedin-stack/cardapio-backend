package br.com.cardapio.modules.cardapio.visualizacao.repository;

import br.com.cardapio.modules.cardapio.visualizacao.model.Visualizacao;
import br.com.cardapio.modules.cardapio.visualizacao.repository.projection.ItemTempoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório de {@link Visualizacao} e da agregação de tempo de tela por item.
 */
@Repository
public interface IVisualizacaoRepository extends JpaRepository<Visualizacao, UUID> {

    /** Todos os itens do restaurante (inclusive sem visualização no período, com zero), do mais visto ao menos. */
    @Query(value = """
            SELECT i.id AS "itemId", i.nome AS "nome",
                   COUNT(v.id) AS "visualizacoes",
                   CAST(COALESCE(SUM(v.duracao_ms), 0) AS BIGINT) AS "tempoTotalMs",
                   CAST(COALESCE(AVG(v.duracao_ms), 0) AS BIGINT) AS "tempoMedioMs"
            FROM public.itens_cardapio i
            LEFT JOIN public.visualizacoes v
                   ON v.item_id = i.id AND v.ocorrido_em >= :inicio AND v.ocorrido_em < :fim
            WHERE i.restaurante_id = :restauranteId
            GROUP BY i.id, i.nome
            ORDER BY "tempoTotalMs" DESC, i.nome
            """, nativeQuery = true)
    List<ItemTempoProjection> findTempoByItem(@Param("restauranteId") UUID restauranteId,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fim") LocalDateTime fim);
}
