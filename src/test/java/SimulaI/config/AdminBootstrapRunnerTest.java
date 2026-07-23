package SimulaI.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import SimulaI.entity.Usuario;
import SimulaI.enums.Role;
import SimulaI.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void naoDeveCriarAdminQuandoJaExisteUm() throws Exception {
        when(usuarioRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                usuarioRepository, passwordEncoder, "admin@simulai.local", "senha-padrao");

        runner.run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveCriarAdminComSenhaCriptografadaQuandoNenhumExiste() throws Exception {
        when(usuarioRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("senha-padrao")).thenReturn("hash-da-senha");

        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                usuarioRepository, passwordEncoder, "admin@simulai.local", "senha-padrao");

        runner.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario adminCriado = captor.getValue();
        assertThat(adminCriado.getEmail()).isEqualTo("admin@simulai.local");
        assertThat(adminCriado.getSenha()).isEqualTo("hash-da-senha");
        assertThat(adminCriado.getRole()).isEqualTo(Role.ADMIN);
    }
}
