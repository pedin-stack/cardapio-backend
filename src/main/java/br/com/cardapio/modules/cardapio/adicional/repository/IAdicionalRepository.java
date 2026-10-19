package br.com.cardapio.modules.cardapio.adicional.repository;

import br.com.cardapio.modules.cardapio.adicional.model.Adicional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Adicional}. Toda consulta recebe o restaurante (isolamento por restaurante);
 * os itens de cada adicional carregam em lote ({@code @BatchSize}).
 */
@Repository
public interface IAdicionalRepository extends JpaRepository<Adicional, UUID> {

    List<Adicional> findByRestauranteId(UUID restauranteId);

    Page<Adicional> findByRestauranteId(UUID restauranteId, Pageable pageable);

    Optional<Adicional> findByIdAndRestauranteId(UUID id, UUID restauranteId);
}
