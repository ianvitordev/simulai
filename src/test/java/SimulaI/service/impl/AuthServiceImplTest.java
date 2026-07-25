package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import SimulaI.dto.ConfirmarCadastroRequestDTO;
import SimulaI.dto.RedefinirSenhaRequestDTO;
import SimulaI.dto.ReenviarCodigoRequestDTO;
import SimulaI.dto.TokenResponseDTO;
import SimulaI.entity.Usuario;
import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.CodigoVerificacaoService;
import SimulaI.service.TokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodigoVerificacaoService codigoVerificacaoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioTeste() {
        return Usuario.builder().id(1L).nome("Maria").email("maria@teste.com").senha("hashAntigo").build();
    }

    @Test
    void deveConfirmarCadastroAtivarContaEDevolverToken() {
        Usuario usuario = usuarioTeste();
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));
        when(tokenService.gerarToken(usuario)).thenReturn("token-jwt");
        when(tokenService.getExpiracaoSegundos()).thenReturn(3600L);

        ConfirmarCadastroRequestDTO request = ConfirmarCadastroRequestDTO.builder()
                .email("maria@teste.com").codigo("123456").build();

        TokenResponseDTO resultado = authService.confirmarCadastro(request);

        verify(codigoVerificacaoService).validarCodigo(usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO, "123456");
        assertThat(usuario.getEmailVerificado()).isTrue();
        assertThat(resultado.getAccessToken()).isEqualTo("token-jwt");
    }

    @Test
    void confirmarCadastroDeveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());
        ConfirmarCadastroRequestDTO request = ConfirmarCadastroRequestDTO.builder()
                .email("inexistente@teste.com").codigo("123456").build();

        assertThatThrownBy(() -> authService.confirmarCadastro(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void esqueciSenhaDeveGerarCodigoQuandoEmailExiste() {
        Usuario usuario = usuarioTeste();
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        authService.esqueciSenha("maria@teste.com");

        verify(codigoVerificacaoService).gerarEEnviar(usuario, TipoCodigoVerificacao.REDEFINICAO_SENHA);
    }

    @Test
    void esqueciSenhaNaoDeveLancarExcecaoQuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

        authService.esqueciSenha("inexistente@teste.com");

        verify(codigoVerificacaoService, never()).gerarEEnviar(any(), any());
    }

    @Test
    void deveRedefinirSenhaEDevolverToken() {
        Usuario usuario = usuarioTeste();
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashNovo");
        when(tokenService.gerarToken(usuario)).thenReturn("token-jwt");
        when(tokenService.getExpiracaoSegundos()).thenReturn(3600L);

        RedefinirSenhaRequestDTO request = RedefinirSenhaRequestDTO.builder()
                .email("maria@teste.com").codigo("123456").novaSenha("novaSenha123").build();

        TokenResponseDTO resultado = authService.redefinirSenha(request);

        verify(codigoVerificacaoService).validarCodigo(usuario, TipoCodigoVerificacao.REDEFINICAO_SENHA, "123456");
        assertThat(usuario.getSenha()).isEqualTo("hashNovo");
        assertThat(resultado.getAccessToken()).isEqualTo("token-jwt");
    }

    @Test
    void reenviarCodigoDeveGerarNovoCodigoDoTipoInformado() {
        Usuario usuario = usuarioTeste();
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        ReenviarCodigoRequestDTO request = ReenviarCodigoRequestDTO.builder()
                .email("maria@teste.com").tipo(TipoCodigoVerificacao.CONFIRMACAO_CADASTRO).build();
        authService.reenviarCodigo(request);

        verify(codigoVerificacaoService).gerarEEnviar(usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO);
    }
}
