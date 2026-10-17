package br.com.cardapio.modules.core.autenticacao.service;

import br.com.cardapio.config.cache.RedisCacheConfig;
import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.infrastructure.service.TokenService;
import br.com.cardapio.infrastructure.service.UserDetailsServiceImpl;
import br.com.cardapio.modules.core.autenticacao.dto.request.LoginRequestDTO;
import br.com.cardapio.modules.core.autenticacao.dto.response.SessaoDTO;
import br.com.cardapio.modules.core.usuario.model.Usuario;
import br.com.cardapio.modules.core.usuario.repository.IUsuarioRepository;
import br.com.cardapio.modules.core.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Login por e-mail e senha. Usuário inexistente e senha errada devolvem a mesma mensagem (anti-enumeração).
 */
@Service
@RequiredArgsConstructor
public class AutenticacaoService implements IAutenticacaoService {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_USER_DETAILS, key = "#dto.email().toLowerCase()")
    public SessaoDTO login(final LoginRequestDTO dto) {
        final Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.email().trim())
                .filter(u -> passwordEncoder.matches(dto.senha(), u.getSenha()))
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.INVALID_CREDENTIALS.getMessage()));

        final UsuarioUserDetailsDTO userDetails = UserDetailsServiceImpl.mapToUserDetailsDTO(usuario);
        if (!userDetails.isEnabled()) {
            throw new BusinessException(BusinessExceptionMessage.USUARIO_INATIVO.getMessage());
        }

        usuario.setUltimoLoginEm(LocalDateTime.now());
        final Instant expiraEm = tokenService.generateExpiration();
        final String token = tokenService.generateToken(usuario.getEmail(), expiraEm);
        return new SessaoDTO(token, expiraEm, UsuarioService.mapToUsuarioSessaoResponseDTO(usuario, expiraEm));
    }
}
