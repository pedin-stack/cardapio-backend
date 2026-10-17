package br.com.cardapio.modules.core.restaurante.repository;

import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Restaurante}.
 */
@Repository
public interface IRestauranteRepository extends JpaRepository<Restaurante, UUID> {

    /** Restaurante desativado (ou inexistente) responde vazio: o cardápio público vira "Serviço indisponível". */
    Optional<Restaurante> findBySlugAndAtivoTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Restaurante> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
