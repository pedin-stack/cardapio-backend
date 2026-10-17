package br.com.cardapio.infrastructure.service;

import br.com.cardapio.config.cache.RedisCacheConfig;
import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.modules.core.usuario.model.Usuario;
import br.com.cardapio.modules.core.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Carrega o principal a partir do e-mail do token; cacheado em {@code userDetails} (TTL 2 min).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.CACHE_USER_DETAILS, key = "#email.toLowerCase()", unless = "#result == null")
    public UsuarioUserDetailsDTO loadUserByUsername(final String email) {
        final Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        return mapToUserDetailsDTO(usuario);
    }

    public static UsuarioUserDetailsDTO mapToUserDetailsDTO(final Usuario usuario) {
        final Set<String> permissoes = usuario.getPerfil().getPermissoes().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new UsuarioUserDetailsDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getStatus(),
                usuario.getPerfil().getNome(),
                usuario.getRestaurante() != null ? usuario.getRestaurante().getId() : null,
                usuario.getRestaurante() != null && usuario.getRestaurante().isAtivo(),
                permissoes);
    }
}
