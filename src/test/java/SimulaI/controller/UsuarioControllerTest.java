package SimulaI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import SimulaI.dto.AlterarRoleRequestDTO;
import SimulaI.dto.UsuarioRequestDTO;
import SimulaI.dto.UsuarioResponseDTO;
import SimulaI.enums.Role;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.service.UsuarioService;

/**
 * Testa a integração Controller -> GlobalExceptionHandler: garante que exceções de
 * negócio lançadas pelo Service (mockado) viram os status HTTP corretos na resposta real.
 */
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioERetornar201() throws Exception {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder().nome("Maria").email("maria@teste.com").senha("123456").build();
        UsuarioResponseDTO response = UsuarioResponseDTO.builder().id(1L).nome("Maria").email("maria@teste.com").role(Role.ALUNO).build();

        when(usuarioService.cadastrar(any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("maria@teste.com"));
    }

    @Test
    void deveRetornar400QuandoCadastroTemCamposInvalidos() throws Exception {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder().nome("").email("email-invalido").senha("").build();

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposInvalidos").isArray());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoEncontrado() throws Exception {
        when(usuarioService.buscarPorId(99L)).thenThrow(RecursoNaoEncontradoException.porId("Usuário", 99L));

        mockMvc.perform(get("/api/usuarios/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRetornar409QuandoEmailDuplicado() throws Exception {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder().nome("Maria").email("maria@teste.com").senha("123456").build();
        when(usuarioService.cadastrar(any(UsuarioRequestDTO.class)))
                .thenThrow(new RegistroDuplicadoException("Já existe um usuário cadastrado com o email: maria@teste.com"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveDeletarUsuarioERetornar204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(usuarioService).deletar(1L);
    }

    @Test
    void devePromoverUsuarioParaAdmin() throws Exception {
        AlterarRoleRequestDTO request = AlterarRoleRequestDTO.builder().role(Role.ADMIN).build();
        UsuarioResponseDTO response = UsuarioResponseDTO.builder().id(1L).nome("Maria").email("maria@teste.com").role(Role.ADMIN).build();

        when(usuarioService.alterarRole(org.mockito.ArgumentMatchers.eq(1L), any(AlterarRoleRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/usuarios/{id}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deveRetornar400QuandoAlterarRoleSemRole() throws Exception {
        mockMvc.perform(patch("/api/usuarios/{id}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
