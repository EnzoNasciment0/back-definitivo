package br.fmc.kaptu.job;

import br.fmc.kaptu.domain.usuario.Administrador;
import br.fmc.kaptu.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o administrador inicial SOMENTE quando KAPTU_ADMIN_EMAIL e
 * KAPTU_ADMIN_SENHA estao definidos (senha com 12+ caracteres).
 * Nao existe senha padrao no codigo (ver SECURITY.md).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private static final int TAMANHO_MINIMO_SENHA = 12;

    private final UsuarioRepository usuarioRepository;
    private final String email;
    private final String senha;

    public DataLoader(
            UsuarioRepository usuarioRepository,
            @Value("${kaptu.admin.email:}") String email,
            @Value("${kaptu.admin.senha:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.email = email == null ? "" : email.trim();
        this.senha = senha == null ? "" : senha;
    }

    @Override
    public void run(String... args) {
        if (email.isEmpty() || senha.isEmpty()) {
            return;
        }

        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            log.warn("KAPTU_ADMIN_SENHA tem menos de {} caracteres: administrador inicial NAO criado.",
                    TAMANHO_MINIMO_SENHA);
            return;
        }

        if (usuarioRepository.existsByEmail(email)) {
            return;
        }

        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
        usuarioRepository.save(new Administrador("Administrador Kaptu", email, senhaHash));
        log.info("Administrador inicial criado.");
    }
}
