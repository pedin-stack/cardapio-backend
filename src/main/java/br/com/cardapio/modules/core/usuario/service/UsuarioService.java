package br.com.cardapio.modules.core.usuario.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import br.com.cardapio.modules.core.usuario.dto.response.UsuarioSessaoResponseDTO;
import br.com.cardapio.modules.core.usuario.model.Usuario;
import br.com.cardapio.modules.core.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Consulta do usuário logado.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UsuarioSessaoResponseDTO findMeusDados(final UsuarioUserDetailsDTO usuarioLogado) {
        final Usuario usuario = usuarioRepository.findByEmailIgnoreCase(usuarioLogado.email())
                .orElseThrow(() -> new BusinessException(BusinessExceptionMessage.NOT_FOUND.getMessage()));
        return mapToUsuarioSessaoResponseDTO(usuario, null);
    }

    /** Usado também pelo login (mesmo formato de resposta). Exige perfil/restaurante carregados. */
    public static UsuarioSessaoResponseDTO mapToUsuarioSessaoResponseDTO(final Usuario usuario, final Instant expiraEm) {
        final Restaurante restaurante = usuario.getRestaurante();
        return new UsuarioSessaoResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().getNome(),
                restaurante != null ? restaurante.getId() : null,
                restaurante != null ? restaurante.getNome() : null,
                restaurante != null ? restaurante.getSlug() : null,
                usuario.getPerfil().getPermissoes().stream().map(Enum::name).collect(Collectors.toSet()),
                expiraEm);
    }
}
