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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import SimulaI.dto.AlterarRoleRequestDTO;
import SimulaI.dto.UsuarioRequestDTO;
import SimulaI.dto.UsuarioResponseDTO;
import SimulaI.entity.Usuario;
import SimulaI.enums.Role;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.mapper.UsuarioMapper;
import SimulaI.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void deveCadastrarUsuarioComSenhaCriptografadaERoleAluno() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nome("Maria").email("maria@teste.com").senha("123456").build();
        Usuario entidade = Usuario.builder().nome("Maria").email("maria@teste.com").build();
        Usuario salvo = Usuario.builder().id(1L).nome("Maria").email("maria@teste.com")
                .senha("hash").role(Role.ALUNO).build();
        UsuarioResponseDTO response = UsuarioResponseDTO.builder().id(1L).nome("Maria")
                .email("maria@teste.com").role(Role.ALUNO).build();

        when(usuarioRepository.existsByEmail("maria@teste.com")).thenReturn(false);
        when(usuarioMapper.toEntity(request)).thenReturn(entidade);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        when(usuarioRepository.save(entidade)).thenReturn(salvo);
        when(usuarioMapper.toResponse(salvo)).thenReturn(response);

        UsuarioResponseDTO resultado = usuarioService.cadastrar(request);

        assertThat(resultado.getRole()).isEqualTo(Role.ALUNO);
        assertThat(entidade.getSenha()).isEqualTo("hash");
        assertThat(entidade.getRole()).isEqualTo(Role.ALUNO);
    }

    @Test
    void deveLancarExcecaoAoCadastrarComEmailDuplicado() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nome("Maria").email("maria@teste.com").senha("123456").build();
        when(usuarioRepository.existsByEmail("maria@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(RegistroDuplicadoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarPorIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoBuscarPorEmailInexistente() {
        when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorEmail("inexistente@teste.com"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveRecriptografarSenhaAoAtualizar() {
        Usuario existente = Usuario.builder().id(1L).nome("Maria").email("maria@teste.com")
                .senha("hashAntigo").role(Role.ALUNO).build();
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nome("Maria Silva").email("maria@teste.com").senha("novaSenha").build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("novaSenha")).thenReturn("hashNovo");
        when(usuarioMapper.toResponse(existente)).thenReturn(
                UsuarioResponseDTO.builder().id(1L).nome("Maria Silva").email("maria@teste.com").role(Role.ALUNO).build());

        usuarioService.atualizar(1L, request);

        ArgumentCaptor<UsuarioRequestDTO> captor = ArgumentCaptor.forClass(UsuarioRequestDTO.class);
        verify(usuarioMapper).updateEntityFromDto(captor.capture(), org.mockito.ArgumentMatchers.eq(existente));
        assertThat(captor.getValue().getNome()).isEqualTo("Maria Silva");
        assertThat(existente.getSenha()).isEqualTo("hashNovo");
    }

    @Test
    void devePromoverUsuarioParaAdmin() {
        Usuario existente = Usuario.builder().id(1L).nome("Maria").email("maria@teste.com").role(Role.ALUNO).build();
        AlterarRoleRequestDTO request = AlterarRoleRequestDTO.builder().role(Role.ADMIN).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioMapper.toResponse(existente)).thenReturn(
                UsuarioResponseDTO.builder().id(1L).nome("Maria").email("maria@teste.com").role(Role.ADMIN).build());

        UsuarioResponseDTO resultado = usuarioService.alterarRole(1L, request);

        assertThat(existente.getRole()).isEqualTo(Role.ADMIN);
        assertThat(resultado.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveLancarExcecaoAoAlterarRoleDeUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.alterarRole(99L, AlterarRoleRequestDTO.builder().role(Role.ADMIN).build()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
