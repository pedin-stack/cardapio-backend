package br.com.cardapio.modules.cardapio.comentario.repository;

import br.com.cardapio.modules.cardapio.comentario.model.Comentario;
import br.com.cardapio.modules.cardapio.comentario.repository.projection.ItemComentariosProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de {@link Comentario} e da contagem por item (cardápio público).
 */
@Repository
public interface IComentarioRepository extends JpaRepository<Comentario, UUID> {

    /** O autor (cliente) vem na mesma consulta: a página mostra o nome de cada um. */
    @EntityGraph(attributePaths = "cliente")
    Page<Comentario> findByItemId(UUID itemId, Pageable pageable);

    Optional<Comentario> findByIdAndItemRestauranteId(UUID id, UUID restauranteId);

    @Query("SELECT c.item.id AS itemId, COUNT(c) AS total "
            + "FROM Comentario c WHERE c.item.restaurante.id = :restauranteId GROUP BY c.item.id")
    List<ItemComentariosProjection> findTotaisByRestauranteId(@Param("restauranteId") UUID restauranteId);
}
