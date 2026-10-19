package br.com.cardapio.modules.cardapio.avaliacao.repository;

import br.com.cardapio.modules.cardapio.avaliacao.model.Avaliacao;
import br.com.cardapio.modules.cardapio.avaliacao.repository.projection.ItemAvaliacaoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de {@link Avaliacao} e da média por item (cardápio público).
 */
@Repository
public interface IAvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    Optional<Avaliacao> findByItemIdAndClienteId(UUID itemId, UUID clienteId);

    /** Média e quantidade de avaliações por item do restaurante (item sem avaliação não aparece). */
    @Query("SELECT a.item.id AS itemId, AVG(a.estrelas) AS media, COUNT(a) AS total "
            + "FROM Avaliacao a WHERE a.item.restaurante.id = :restauranteId GROUP BY a.item.id")
    List<ItemAvaliacaoProjection> findMediasByRestauranteId(@Param("restauranteId") UUID restauranteId);
}
