package br.com.cardapio.modules.core.perfil.repository;

import br.com.cardapio.modules.core.perfil.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Perfil}.
 */
@Repository
public interface IPerfilRepository extends JpaRepository<Perfil, UUID> {

    Optional<Perfil> findByNome(String nome);
}
