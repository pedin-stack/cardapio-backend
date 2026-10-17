package br.com.cardapio.config.dataLoader.core;

import br.com.cardapio.modules.core.perfil.model.Perfil;
import br.com.cardapio.modules.core.perfil.repository.IPerfilRepository;
import br.com.cardapio.modules.core.usuario.model.Usuario;
import br.com.cardapio.modules.core.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o primeiro Super Administrador a partir de {@code SUPER_ADMIN_EMAIL}/{@code SUPER_ADMIN_SENHA}.
 * <p>
 * Desvio do blueprint §6.4.8 (lá é SQL manual): sem cadastro público não há conta para promover (ADR-0001).
 * Não faz nada se já existir um super admin ou se as variáveis estiverem vazias — a senha nunca fica em migration.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdministradorDataLoader implements ApplicationRunner {

    private final IUsuarioRepository usuarioRepository;
    private final IPerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.email}")
    private String email;

    @Value("${app.super-admin.senha}")
    private String senha;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        if (usuarioRepository.existsByPerfilNome(Perfil.SUPER_ADMINISTRADOR)) {
            return;
        }
        if (email == null || email.isBlank() || senha == null || senha.length() < 8) {
            log.warn("Nenhum Super Administrador cadastrado: defina SUPER_ADMIN_EMAIL e SUPER_ADMIN_SENHA (mín. 8 caracteres).");
            return;
        }
        final Perfil perfil = perfilRepository.findByNome(Perfil.SUPER_ADMINISTRADOR)
                .orElseThrow(() -> new IllegalStateException("Perfil Super Administrador ausente: migrations não aplicadas."));

        final Usuario usuario = new Usuario();
        usuario.setNome("Super Administrador");
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
        log.info("Super Administrador inicial criado.");
    }
}
