package SimulaI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import SimulaI.dto.LoginRequestDTO;
import SimulaI.entity.Usuario;
import SimulaI.enums.Role;
import SimulaI.service.TokenService;
import SimulaI.service.UsuarioDetailsImpl;

/**
 * addFilters=false: o SecurityConfig real (que libera /api/auth/** via permitAll) não é
 * carregado nesta fatia @WebMvcTest, então desligamos os filtros para focar no
 * comportamento do Controller em si (delega ao AuthenticationManager e ao TokenService).
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void deveAutenticarERetornarToken() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder().email("maria@teste.com").senha("123456").build();
        Usuario usuario = Usuario.builder().id(1L).nome("Maria").email("maria@teste.com").role(Role.ALUNO).build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UsuarioDetailsImpl(usuario), null, new UsuarioDetailsImpl(usuario).getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenService.gerarToken(usuario)).thenReturn("token-jwt-fake");
        when(tokenService.getExpiracaoSegundos()).thenReturn(3600L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-jwt-fake"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEmSegundos").value(3600));
    }

    @Test
    void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder().email("maria@teste.com").senha("senhaErrada").build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder().email("nao-e-email").senha("123456").build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
