package br.com.cardapio.config.security.dto;

import br.com.cardapio.modules.core.usuario.enums.StatusUsuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Principal do Spring Security (serializável: vai para o cache Redis {@code userDetails}).
 * <p>
 * {@code restauranteId} isola os dados do gestor (IDOR, blueprint §6.9); {@code restauranteAtivo}
 * corta o acesso do gestor quando o super admin desativa o restaurante.
 * </p>
 */
// "username" é derivado de email (getUsername): sem ignorá-lo, ler o cache do Redis falharia por propriedade desconhecida.
@JsonIgnoreProperties({"username", "accountNonLocked", "accountNonExpired", "credentialsNonExpired", "enabled", "authorities"})
public record UsuarioUserDetailsDTO(
        UUID id,
        String nome,
        String email,
        String password,
        StatusUsuario status,
        String perfilNome,
        UUID restauranteId,
        boolean restauranteAtivo,
        Set<String> permissoes
) implements UserDetails, Serializable {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissoes == null
                ? Set.of()
                : permissoes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return status == StatusUsuario.ATIVO && (restauranteId == null || restauranteAtivo);
    }
}
