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
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import SimulaI.entity.Assunto;
import SimulaI.entity.Banca;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Questao;
import SimulaI.exception.GeracaoIAException;
import SimulaI.exception.RegraNegocioException;

@ExtendWith(MockitoExtension.class)
class IndexadorConteudoIAImplTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono<byte[]> monoBytes;

    private IndexadorConteudoIAImpl indexadorConteudoIA;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        indexadorConteudoIA = new IndexadorConteudoIAImpl(vectorStore, webClientBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveIndexarQuestaoComMetadadosCorretos() {
        Disciplina disciplina = Disciplina.builder().id(1L).nome("Direito Administrativo").build();
        Assunto assunto = Assunto.builder().id(2L).nome("Atos Administrativos").build();
        Questao questao = Questao.builder().id(10L)
                .enunciado("Enunciado da questão")
                .explicacao("Explicação da resposta")
                .disciplina(disciplina)
                .assunto(assunto)
                .build();

        indexadorConteudoIA.indexarQuestao(questao);

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        Document documento = captor.getValue().get(0);
        assertThat(documento.getText()).contains("Enunciado da questão").contains("Explicação da resposta");
        assertThat(documento.getMetadata()).containsEntry("tipo", "questao_historica");
        assertThat(documento.getMetadata()).containsEntry("questaoId", 10L);
        assertThat(documento.getMetadata()).containsEntry("disciplinaId", 1L);
        assertThat(documento.getMetadata()).containsEntry("assuntoId", 2L);
    }

    @Test
    void deveLancarExcecaoAoIndexarEditalSemUrlCadastrada() {
        Concurso concurso = Concurso.builder().id(1L).editalUrl(null).build();

        assertThatThrownBy(() -> indexadorConteudoIA.indexarEdital(concurso))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoAoIndexarEditalComUrlEmBranco() {
        Concurso concurso = Concurso.builder().id(1L).editalUrl("   ").build();

        assertThatThrownBy(() -> indexadorConteudoIA.indexarEdital(concurso))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveEncapsularFalhaDeDownloadEmGeracaoIAException() {
        Banca banca = Banca.builder().id(1L).nome("CESPE").build();
        Concurso concurso = Concurso.builder().id(1L).editalUrl("http://exemplo.com/edital.pdf").banca(banca).build();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(monoBytes);
        when(monoBytes.block()).thenThrow(new RuntimeException("conexão recusada"));

        assertThatThrownBy(() -> indexadorConteudoIA.indexarEdital(concurso))
                .isInstanceOf(GeracaoIAException.class);
    }
}
