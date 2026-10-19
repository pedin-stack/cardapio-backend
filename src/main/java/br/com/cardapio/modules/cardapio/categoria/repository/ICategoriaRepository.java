package br.com.cardapio.modules.cardapio.categoria.repository;

import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Categoria}. Toda consulta recebe o restaurante (isolamento por restaurante).
 */
@Repository
public interface ICategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findByRestauranteIdOrderByOrdemAscNomeAsc(UUID restauranteId);

    Page<Categoria> findByRestauranteId(UUID restauranteId, Pageable pageable);

    List<Categoria> findByIdInAndRestauranteId(Collection<UUID> ids, UUID restauranteId);

    Optional<Categoria> findByIdAndRestauranteId(UUID id, UUID restauranteId);

    boolean existsByRestauranteIdAndNomeIgnoreCase(UUID restauranteId, String nome);

    boolean existsByRestauranteIdAndNomeIgnoreCaseAndIdNot(UUID restauranteId, String nome, UUID id);
}
