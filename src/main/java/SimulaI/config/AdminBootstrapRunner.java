package SimulaI.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import SimulaI.entity.Usuario;
import SimulaI.enums.Role;
import SimulaI.repository.UsuarioRepository;

/**
 * Sem isso, é impossível criar o primeiro ADMIN: o cadastro público
 * (POST /api/usuarios) sempre força role ALUNO, e não existe outro caminho para
 * alcançar os endpoints ADMIN-only (curadoria de catálogo, geração de questões via IA).
 * Roda a cada boot, mas só age se nenhum ADMIN existir ainda — idempotente.
 *
 * @Lazy(false): com "spring.main.lazy-initialization: true" ligado globalmente (ver
 * application.yaml), um ApplicationRunner comum viraria lazy também e nunca seria
 * instanciado/executado sozinho no boot — precisa forçar eager aqui.
 */
@Slf4j
@Component
@Lazy(false)
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminSenha;

    public AdminBootstrapRunner(UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${simulai.admin.email:admin@simulai.local}") String adminEmail,
                                 @Value("${simulai.admin.senha:troque-esta-senha-no-primeiro-login}") String adminSenha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        Usuario admin = Usuario.builder()
                .nome("Administrador")
                .email(adminEmail)
                .senha(passwordEncoder.encode(adminSenha))
                .role(Role.ADMIN)
                .emailVerificado(true)
                .build();

        usuarioRepository.save(admin);

        log.warn("Nenhum usuário ADMIN encontrado — criado automaticamente (email: {}). "
                + "Faça login e troque a senha imediatamente via PUT /api/usuarios/{{id}}.", adminEmail);
    }
}
