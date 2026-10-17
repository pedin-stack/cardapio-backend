package br.com.cardapio.modules.core.usuario.repository;

import br.com.cardapio.modules.core.usuario.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Usuario}.
 */
@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, UUID> {

    /** Carrega perfil, permissões e restaurante juntos: é a consulta do filtro JWT (evita N+1). */
    @EntityGraph(attributePaths = {"perfil", "perfil.permissoes", "restaurante"})
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPerfilNome(String nomePerfil);

    List<Usuario> findByRestauranteId(UUID restauranteId);
}
