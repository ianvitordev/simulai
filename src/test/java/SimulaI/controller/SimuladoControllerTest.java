package SimulaI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import SimulaI.dto.GerarSimuladoRequestDTO;
import SimulaI.dto.RespostaUsuarioRequestDTO;
import SimulaI.dto.RespostaUsuarioResponseDTO;
import SimulaI.dto.RevisaoAlternativaDTO;
import SimulaI.dto.RevisaoQuestaoDTO;
import SimulaI.dto.SimuladoResponseDTO;
import SimulaI.dto.SimuladoResultadoDTO;
import SimulaI.dto.SimuladoRevisaoDTO;
import SimulaI.entity.Usuario;
import SimulaI.enums.Role;
import SimulaI.enums.StatusSimulado;
import SimulaI.exception.RegraNegocioException;
import SimulaI.service.SimuladoService;
import SimulaI.service.UsuarioDetailsImpl;

/**
 * addFilters=false mantém o foco original destes testes (Controller -> GlobalExceptionHandler),
 * já que o SecurityConfig real (com @PreAuthorize/@EnableMethodSecurity) não é carregado
 * nesta fatia @WebMvcTest. O endpoint /gerar lê o principal diretamente no corpo do método,
 * então esse único teste injeta um UsuarioDetailsImpl real via RequestPostProcessor.
 */
@WebMvcTest(SimuladoController.class)
@AutoConfigureMockMvc(addFilters = false)
class SimuladoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SimuladoService simuladoService;

    /**
     * addFilters=false desliga toda a cadeia de filtros do Security, inclusive o que leria
     * a autenticação da sessão de volta para o SecurityContextHolder — por isso os
     * RequestPostProcessors prontos do spring-security-test (que dependem desse filtro)
     * não funcionam aqui. Setar o SecurityContextHolder direto na thread do teste é lido
     * por @AuthenticationPrincipal independentemente de filtros.
     */
    private void autenticarComo(Long usuarioId) {
        Usuario usuario = Usuario.builder().id(usuarioId).email("usuario@teste.com").role(Role.ALUNO).build();
        UsuarioDetailsImpl principal = new UsuarioDetailsImpl(usuario);
        Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveGerarSimuladoERetornar201() throws Exception {
        autenticarComo(1L);
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(10).build();
        SimuladoResponseDTO response = SimuladoResponseDTO.builder().id(1L).status(StatusSimulado.CRIADO).build();

        when(simuladoService.gerar(eq(1L), any(GerarSimuladoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/simulados/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornar422QuandoBancoDeQuestoesInsuficiente() throws Exception {
        autenticarComo(1L);
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(999).build();

        when(simuladoService.gerar(eq(1L), any(GerarSimuladoRequestDTO.class)))
                .thenThrow(new RegraNegocioException("Banco de questões insuficiente."));

        mockMvc.perform(post("/api/simulados/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornar400QuandoQuantidadeQuestoesInvalida() throws Exception {
        autenticarComo(1L);
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(-1).build();

        mockMvc.perform(post("/api/simulados/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveIniciarSimulado() throws Exception {
        when(simuladoService.iniciar(1L)).thenReturn(SimuladoResponseDTO.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).build());

        mockMvc.perform(patch("/api/simulados/{id}/iniciar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    void deveResponderQuestao() throws Exception {
        RespostaUsuarioRequestDTO request = RespostaUsuarioRequestDTO.builder()
                .questaoId(10L).alternativaMarcadaId(100L).tempoRespostaSegundos(30).build();
        RespostaUsuarioResponseDTO response = RespostaUsuarioResponseDTO.builder().id(1L).acertou(true).build();

        when(simuladoService.responderQuestao(eq(1L), any(RespostaUsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/simulados/{id}/respostas", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acertou").value(true));
    }

    @Test
    void deveFinalizarSimulado() throws Exception {
        SimuladoResultadoDTO resultado = SimuladoResultadoDTO.builder()
                .simuladoId(1L).totalQuestoes(10).acertos(7).erros(3).percentualAcerto(70.0).tempoTotalSegundos(600).build();
        when(simuladoService.finalizar(1L)).thenReturn(resultado);

        mockMvc.perform(patch("/api/simulados/{id}/finalizar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acertos").value(7));
    }

    @Test
    void deveCancelarSimulado() throws Exception {
        when(simuladoService.cancelar(1L)).thenReturn(SimuladoResponseDTO.builder().id(1L).status(StatusSimulado.CANCELADO).build());

        mockMvc.perform(patch("/api/simulados/{id}/cancelar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void deveRevisarSimuladoFinalizado() throws Exception {
        RevisaoAlternativaDTO alternativa = RevisaoAlternativaDTO.builder()
                .id(100L).descricao("Certo").correta(true).marcadaPeloUsuario(true).build();
        RevisaoQuestaoDTO questao = RevisaoQuestaoDTO.builder()
                .questaoId(10L).enunciado("Enunciado").respondida(true).acertou(true)
                .alternativas(java.util.List.of(alternativa)).build();
        SimuladoRevisaoDTO revisao = SimuladoRevisaoDTO.builder().simuladoId(1L).questoes(java.util.List.of(questao)).build();

        when(simuladoService.revisar(1L)).thenReturn(revisao);

        mockMvc.perform(get("/api/simulados/{id}/revisao", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simuladoId").value(1))
                .andExpect(jsonPath("$.questoes[0].acertou").value(true))
                .andExpect(jsonPath("$.questoes[0].alternativas[0].correta").value(true));
    }

    @Test
    void deveRetornar422QuandoRevisarSimuladoNaoFinalizado() throws Exception {
        when(simuladoService.revisar(1L))
                .thenThrow(new RegraNegocioException("A revisão só fica disponível depois de finalizar o simulado."));

        mockMvc.perform(get("/api/simulados/{id}/revisao", 1L))
                .andExpect(status().isUnprocessableEntity());
    }
}
