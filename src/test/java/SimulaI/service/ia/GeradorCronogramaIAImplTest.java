package SimulaI.service.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import SimulaI.exception.GeracaoIAException;

@ExtendWith(MockitoExtension.class)
class GeradorCronogramaIAImplTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private GeradorCronogramaIAImpl geradorCronogramaIA;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        geradorCronogramaIA = new GeradorCronogramaIAImpl(chatClientBuilder);
    }

    private List<OpcaoEstudoIA> opcoesTeste() {
        return List.of(
                new OpcaoEstudoIA("Português", "Crase", 40.0, 10),
                new OpcaoEstudoIA("Matemática", "Porcentagem", 90.0, 10));
    }

    @Test
    void deveGerarCronogramaValidoComItensDentroDasOpcoesInformadas() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Foque em Crase, que está mais fraco.",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Português", "Crase", 60, "Revisão de crase",
                        "Percentual de acerto baixo (40%).")));

        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        CronogramaGeradoIA resultado = geradorCronogramaIA.gerar(opcoesTeste(), 3, 1);

        assertThat(resultado.itens()).hasSize(1);
        assertThat(resultado.itens().get(0).disciplina()).isEqualTo("Português");
    }

    @Test
    void deveAceitarDiaDaSemanaComAcentoOuMinuscula() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Observação",
                List.of(new ItemCronogramaGeradoIA("Terça", "Português", "Crase", 60, "Foco", "Justificativa")));

        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        CronogramaGeradoIA resultado = geradorCronogramaIA.gerar(opcoesTeste(), 3, 1);

        assertThat(resultado.itens()).hasSize(1);
    }

    @Test
    void deveIncluirOpcoesEDisponibilidadeNoPrompt() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Observação",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Português", "Crase", 60, "Foco", "Justificativa")));

        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        geradorCronogramaIA.gerar(opcoesTeste(), 4, 2);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("4 dia(s)")
                .contains("Crase")
                .contains("Porcentagem");
    }

    @Test
    void deveLancarExcecaoQuandoIaNaoRetornaItens() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA("Observação", List.of());
        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorCronogramaIA.gerar(opcoesTeste(), 3, 1))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoDiaDaSemanaInvalido() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Observação",
                List.of(new ItemCronogramaGeradoIA("FERIADO", "Português", "Crase", 60, "Foco", "Justificativa")));
        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorCronogramaIA.gerar(opcoesTeste(), 3, 1))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoDuracaoInvalida() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Observação",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Português", "Crase", 0, "Foco", "Justificativa")));
        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorCronogramaIA.gerar(opcoesTeste(), 3, 1))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoDisciplinaOuAssuntoForaDaListaInformada() {
        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Observação",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Inglês", "Verb Tenses", 60, "Foco", "Justificativa")));
        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorCronogramaIA.gerar(opcoesTeste(), 3, 1))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoIaNaoRetornaConteudo() {
        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenReturn(null);

        assertThatThrownBy(() -> geradorCronogramaIA.gerar(opcoesTeste(), 3, 1))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveEncapsularErroDeChamadaAIaEmGeracaoIAException() {
        when(callResponseSpec.entity(CronogramaGeradoIA.class)).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> geradorCronogramaIA.gerar(opcoesTeste(), 3, 1))
                .isInstanceOf(GeracaoIAException.class);
    }
}
