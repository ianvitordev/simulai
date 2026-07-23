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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import SimulaI.dto.GerarQuestaoIARequestDTO;
import SimulaI.dto.QuestaoResponseDTO;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.TipoQuestao;
import SimulaI.exception.GeracaoIAException;
import SimulaI.service.QuestaoService;

/**
 * addFilters=false: mesma razão dos demais Controllers de Security — o SecurityConfig
 * real (que restringe /gerar-ia a ADMIN) não é carregado nesta fatia @WebMvcTest.
 */
@WebMvcTest(QuestaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestaoService questaoService;

    private GerarQuestaoIARequestDTO requestValido() {
        return GerarQuestaoIARequestDTO.builder()
                .disciplinaId(1L)
                .assuntoId(2L)
                .dificuldade(Dificuldade.MEDIA)
                .tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .quantidade(1)
                .build();
    }

    @Test
    void deveGerarQuestoesViaIAERetornar201() throws Exception {
        QuestaoResponseDTO response = QuestaoResponseDTO.builder().id(1L).enunciado("Enunciado gerado").build();
        when(questaoService.gerarViaIA(any(GerarQuestaoIARequestDTO.class))).thenReturn(java.util.List.of(response));

        mockMvc.perform(post("/api/questoes/gerar-ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].enunciado").value("Enunciado gerado"));
    }

    @Test
    void deveRetornar400QuandoQuantidadeAusente() throws Exception {
        GerarQuestaoIARequestDTO request = requestValido();
        request.setQuantidade(null);

        mockMvc.perform(post("/api/questoes/gerar-ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoQuantidadeExcedeLimite() throws Exception {
        GerarQuestaoIARequestDTO request = requestValido();
        request.setQuantidade(11);

        mockMvc.perform(post("/api/questoes/gerar-ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar502QuandoIaFalha() throws Exception {
        when(questaoService.gerarViaIA(any(GerarQuestaoIARequestDTO.class)))
                .thenThrow(new GeracaoIAException("Falha ao chamar o provedor de IA: timeout"));

        mockMvc.perform(post("/api/questoes/gerar-ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadGateway());
    }
}
