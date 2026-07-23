package SimulaI.service.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import SimulaI.entity.Assunto;
import SimulaI.entity.Banca;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.LetraAlternativa;
import SimulaI.enums.TipoQuestao;
import SimulaI.exception.GeracaoIAException;

@ExtendWith(MockitoExtension.class)
class GeradorQuestaoIAImplTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private VectorStore vectorStore;

    private GeradorQuestaoIAImpl geradorQuestaoIA;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        lenient().when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        geradorQuestaoIA = new GeradorQuestaoIAImpl(chatClientBuilder, vectorStore);
    }

    private Disciplina disciplinaTeste() {
        return Disciplina.builder().id(1L).nome("Direito Administrativo").build();
    }

    private Assunto assuntoTeste() {
        return Assunto.builder().id(2L).nome("Atos Administrativos").build();
    }

    @Test
    void deveGerarQuestaoMultiplaEscolhaComCincoAlternativasEUmaCorreta() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado gerado pela IA",
                "Comentário",
                "Explicação da resposta correta",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "alternativa a", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "alternativa b", false),
                        new AlternativaGeradaIA(LetraAlternativa.C, "alternativa c", false),
                        new AlternativaGeradaIA(LetraAlternativa.D, "alternativa d", false),
                        new AlternativaGeradaIA(LetraAlternativa.E, "alternativa e", false)));

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        QuestaoGeradaIA resultado = geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.MULTIPLA_ESCOLHA, null, null, List.of());

        assertThat(resultado.enunciado()).isEqualTo("Enunciado gerado pela IA");
        assertThat(resultado.alternativas()).hasSize(5);
    }

    @Test
    void deveGerarQuestaoCertoErradoComDuasAlternativas() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado certo/errado",
                "Comentário",
                "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        QuestaoGeradaIA resultado = geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.FACIL, TipoQuestao.CERTO_ERRADO, null, null, List.of());

        assertThat(resultado.alternativas()).hasSize(2);
    }

    @Test
    void deveGerarQuestaoDiscursivaSemAlternativas() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Disserte sobre...", "Comentário", "Pontos esperados na resposta", List.of());

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        QuestaoGeradaIA resultado = geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.DIFICIL, TipoQuestao.DISCURSIVA, null, null, List.of());

        assertThat(resultado.alternativas()).isEmpty();
    }

    @Test
    void deveIncluirEstiloDaBancaNoPromptQuandoConcursoInformado() {
        Banca banca = Banca.builder().id(1L).nome("CESPE").build();
        Concurso concurso = Concurso.builder().id(1L).cargo("Analista Judiciário").banca(banca).build();

        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        geradorQuestaoIA.gerar(disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO, concurso, null, List.of());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("CESPE").contains("Analista Judiciário");
    }

    @Test
    void deveIncluirContextoDeEditalNoPromptQuandoEditalConcursoIdInformado() {
        Document trechoEdital = Document.builder().text("Conteúdo programático: Direito Administrativo.").build();
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(trechoEdital));

        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));
        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        geradorQuestaoIA.gerar(disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO, null, 10L, List.of());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("Conteúdo programático: Direito Administrativo.");

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore, org.mockito.Mockito.times(1)).similaritySearch(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getFilterExpression().toString()).contains("edital");
    }

    @Test
    void naoDeveConsultarVectorStoreQuandoEditalConcursoIdNulo() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));
        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        geradorQuestaoIA.gerar(disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO, null, null, List.of());

        // sem editalConcursoId não há motivo pra consultar o vector store — o contexto
        // anti-repetição agora vem direto do parâmetro enunciadosExistentes, sem embeddings.
        verify(vectorStore, org.mockito.Mockito.never()).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void deveIncluirEnunciadosExistentesNoPromptComInstrucaoDeNaoRepetir() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado novo", "Comentário", "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));
        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        List<String> enunciadosExistentes = List.of(
                "Questão antiga 1 sobre atos administrativos.",
                "Questão antiga 2 sobre atos administrativos.");

        geradorQuestaoIA.gerar(disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO,
                null, null, enunciadosExistentes);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("Questão antiga 1 sobre atos administrativos.")
                .contains("Questão antiga 2 sobre atos administrativos.")
                .contains("NÃO repita");
    }

    @Test
    void deveDegradarGraciosamenteQuandoVectorStoreFalha() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenThrow(new RuntimeException("indisponível"));

        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));
        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        QuestaoGeradaIA resultado = geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO, null, 10L, List.of());

        assertThat(resultado).isNotNull();
    }

    @Test
    void deveLancarExcecaoQuandoIaRetornaQuantidadeDeAlternativasIncompativel() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "Explicação",
                List.of(new AlternativaGeradaIA(LetraAlternativa.A, "só uma", true)));

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.MULTIPLA_ESCOLHA, null, null, List.of()))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoIaRetornaExplicacaoEmBranco() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "  ",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO, null, null, List.of()))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoIaMarcaMaisDeUmaAlternativaComoCorreta() {
        QuestaoGeradaIA respostaIA = new QuestaoGeradaIA(
                "Enunciado", "Comentário", "Explicação",
                List.of(
                        new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", true)));

        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(respostaIA);

        assertThatThrownBy(() -> geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.CERTO_ERRADO, null, null, List.of()))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveLancarExcecaoQuandoIaNaoRetornaConteudo() {
        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenReturn(null);

        assertThatThrownBy(() -> geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.MULTIPLA_ESCOLHA, null, null, List.of()))
                .isInstanceOf(GeracaoIAException.class);
    }

    @Test
    void deveEncapsularErroDeChamadaAIaEmGeracaoIAException() {
        when(callResponseSpec.entity(QuestaoGeradaIA.class)).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> geradorQuestaoIA.gerar(
                disciplinaTeste(), assuntoTeste(), Dificuldade.MEDIA, TipoQuestao.MULTIPLA_ESCOLHA, null, null, List.of()))
                .isInstanceOf(GeracaoIAException.class);
    }
}
