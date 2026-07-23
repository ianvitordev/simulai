package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.AlternativaRequestDTO;
import SimulaI.dto.GerarQuestaoIARequestDTO;
import SimulaI.dto.QuestaoRequestDTO;
import SimulaI.dto.QuestaoResponseDTO;
import SimulaI.entity.Alternativa;
import SimulaI.entity.Assunto;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Questao;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.LetraAlternativa;
import SimulaI.enums.TipoQuestao;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.AlternativaMapper;
import SimulaI.mapper.QuestaoMapper;
import SimulaI.repository.AssuntoRepository;
import SimulaI.repository.ConcursoRepository;
import SimulaI.repository.DisciplinaRepository;
import SimulaI.repository.QuestaoRepository;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.ia.AlternativaGeradaIA;
import SimulaI.service.ia.GeradorQuestaoIA;
import SimulaI.service.ia.IndexadorConteudoIA;
import SimulaI.service.ia.QuestaoGeradaIA;

@ExtendWith(MockitoExtension.class)
class QuestaoServiceImplTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private ConcursoRepository concursoRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private AssuntoRepository assuntoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private QuestaoMapper questaoMapper;

    @Mock
    private AlternativaMapper alternativaMapper;

    @Mock
    private IndexadorConteudoIA indexadorConteudoIA;

    @Mock
    private GeradorQuestaoIA geradorQuestaoIA;

    @InjectMocks
    private QuestaoServiceImpl questaoService;

    private QuestaoRequestDTO montarRequestObjetiva(List<AlternativaRequestDTO> alternativas) {
        return QuestaoRequestDTO.builder()
                .enunciado("Enunciado")
                .ano(2024)
                .dificuldade(Dificuldade.MEDIA)
                .tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .disciplinaId(1L)
                .assuntoId(2L)
                .alternativas(alternativas)
                .build();
    }

    @Test
    void deveCadastrarQuestaoObjetivaComUmaAlternativaCorreta() {
        AlternativaRequestDTO correta = AlternativaRequestDTO.builder().letra(LetraAlternativa.A).descricao("a").correta(true).build();
        AlternativaRequestDTO errada = AlternativaRequestDTO.builder().letra(LetraAlternativa.B).descricao("b").correta(false).build();
        QuestaoRequestDTO request = montarRequestObjetiva(List.of(correta, errada));

        Disciplina disciplina = Disciplina.builder().id(1L).nome("D").build();
        Assunto assunto = Assunto.builder().id(2L).nome("A").build();
        Questao entidade = Questao.builder().enunciado("Enunciado").build();
        Questao salva = Questao.builder().id(1L).enunciado("Enunciado").build();
        QuestaoResponseDTO response = QuestaoResponseDTO.builder().id(1L).enunciado("Enunciado").build();

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(assuntoRepository.findById(2L)).thenReturn(Optional.of(assunto));
        when(questaoMapper.toEntity(request)).thenReturn(entidade);
        when(questaoRepository.save(entidade)).thenReturn(salva);
        when(questaoMapper.toResponse(salva)).thenReturn(response);

        QuestaoResponseDTO resultado = questaoService.cadastrar(request);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(entidade.getDisciplina()).isEqualTo(disciplina);
        assertThat(entidade.getAssunto()).isEqualTo(assunto);
        assertThat(entidade.getConcurso()).isNull();
        assertThat(entidade.getUsuario()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoQuestaoObjetivaSemAlternativas() {
        QuestaoRequestDTO request = montarRequestObjetiva(List.of());

        assertThatThrownBy(() -> questaoService.cadastrar(request))
                .isInstanceOf(RegraNegocioException.class);

        verify(questaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoQuestaoObjetivaComMaisDeUmaAlternativaCorreta() {
        AlternativaRequestDTO a = AlternativaRequestDTO.builder().letra(LetraAlternativa.A).descricao("a").correta(true).build();
        AlternativaRequestDTO b = AlternativaRequestDTO.builder().letra(LetraAlternativa.B).descricao("b").correta(true).build();
        QuestaoRequestDTO request = montarRequestObjetiva(List.of(a, b));

        assertThatThrownBy(() -> questaoService.cadastrar(request))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoQuandoQuestaoObjetivaSemAlternativaCorreta() {
        AlternativaRequestDTO a = AlternativaRequestDTO.builder().letra(LetraAlternativa.A).descricao("a").correta(false).build();
        AlternativaRequestDTO b = AlternativaRequestDTO.builder().letra(LetraAlternativa.B).descricao("b").correta(false).build();
        QuestaoRequestDTO request = montarRequestObjetiva(List.of(a, b));

        assertThatThrownBy(() -> questaoService.cadastrar(request))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveCadastrarQuestaoDiscursivaSemExigirAlternativas() {
        QuestaoRequestDTO request = QuestaoRequestDTO.builder()
                .enunciado("Disserte sobre...")
                .ano(2024)
                .dificuldade(Dificuldade.DIFICIL)
                .tipo(TipoQuestao.DISCURSIVA)
                .disciplinaId(1L)
                .assuntoId(2L)
                .alternativas(null)
                .build();

        Disciplina disciplina = Disciplina.builder().id(1L).build();
        Assunto assunto = Assunto.builder().id(2L).build();
        Questao entidade = Questao.builder().enunciado("Disserte sobre...").build();
        Questao salva = Questao.builder().id(1L).build();

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(assuntoRepository.findById(2L)).thenReturn(Optional.of(assunto));
        when(questaoMapper.toEntity(request)).thenReturn(entidade);
        when(questaoRepository.save(entidade)).thenReturn(salva);
        when(questaoMapper.toResponse(salva)).thenReturn(QuestaoResponseDTO.builder().id(1L).build());

        assertThat(questaoService.cadastrar(request)).isNotNull();
    }

    @Test
    void deveGerarQuestoesViaIAEDelegarParaCadastrarComMetadadosCorretos() {
        Disciplina disciplina = Disciplina.builder().id(1L).nome("D").build();
        Assunto assunto = Assunto.builder().id(2L).nome("A").build();

        GerarQuestaoIARequestDTO request = GerarQuestaoIARequestDTO.builder()
                .disciplinaId(1L).assuntoId(2L)
                .dificuldade(Dificuldade.MEDIA).tipo(TipoQuestao.CERTO_ERRADO)
                .quantidade(2).build();

        QuestaoGeradaIA questaoGerada = new QuestaoGeradaIA(
                "Enunciado IA", "Comentário", "Explicação",
                List.of(new AlternativaGeradaIA(LetraAlternativa.A, "Certo", true),
                        new AlternativaGeradaIA(LetraAlternativa.B, "Errado", false)));

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(assuntoRepository.findById(2L)).thenReturn(Optional.of(assunto));
        when(questaoRepository.findByAssuntoOrderByIdDesc(assunto)).thenReturn(List.of());

        // a lista de enunciados existentes é a MESMA instância mutável reaproveitada a
        // cada iteração do lote (cresce ao longo do loop) — um ArgumentCaptor comum
        // capturaria só a referência, mostrando o estado final em ambas as chamadas.
        // Por isso tiramos uma cópia defensiva no momento exato de cada chamada.
        List<List<String>> enunciadosPorChamada = new ArrayList<>();
        when(geradorQuestaoIA.gerar(any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    List<String> argumento = invocation.getArgument(6);
                    enunciadosPorChamada.add(new ArrayList<>(argumento));
                    return questaoGerada;
                });

        Questao entidade = Questao.builder().enunciado("Enunciado IA").build();
        Questao salva = Questao.builder().id(1L).enunciado("Enunciado IA").build();
        when(questaoMapper.toEntity(any(QuestaoRequestDTO.class))).thenReturn(entidade);
        when(questaoRepository.save(entidade)).thenReturn(salva);
        when(questaoMapper.toResponse(salva))
                .thenReturn(QuestaoResponseDTO.builder().id(1L).enunciado("Enunciado IA").build());

        List<QuestaoResponseDTO> resultado = questaoService.gerarViaIA(request);

        assertThat(resultado).hasSize(2);
        verify(geradorQuestaoIA, times(2)).gerar(eq(disciplina), eq(assunto), eq(Dificuldade.MEDIA),
                eq(TipoQuestao.CERTO_ERRADO), isNull(), isNull(), any());

        // a 1ª chamada não vê nenhum enunciado ainda; a 2ª já enxerga o enunciado gerado
        // pela 1ª — é assim que evitamos quase-duplicatas dentro do mesmo lote.
        assertThat(enunciadosPorChamada.get(0)).isEmpty();
        assertThat(enunciadosPorChamada.get(1)).containsExactly("Enunciado IA");

        ArgumentCaptor<QuestaoRequestDTO> captor = ArgumentCaptor.forClass(QuestaoRequestDTO.class);
        verify(questaoMapper, times(2)).toEntity(captor.capture());
        QuestaoRequestDTO dtoGerado = captor.getAllValues().get(0);

        assertThat(dtoGerado.getGeradaPorIA()).isTrue();
        assertThat(dtoGerado.getFonte()).isEqualTo("Gerado por IA");
        assertThat(dtoGerado.getAno()).isEqualTo(Year.now().getValue());
        assertThat(dtoGerado.getDisciplinaId()).isEqualTo(1L);
        assertThat(dtoGerado.getAssuntoId()).isEqualTo(2L);
        assertThat(dtoGerado.getAlternativas()).hasSize(2);
    }

    @Test
    void deveLancarExcecaoAoBuscarQuestaoInexistente() {
        when(questaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questaoService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoAtualizarQuestaoObjetivaSemAlternativaCorreta() {
        QuestaoRequestDTO request = montarRequestObjetiva(
                List.of(AlternativaRequestDTO.builder().letra(LetraAlternativa.A).descricao("a").correta(false).build()));

        assertThatThrownBy(() -> questaoService.atualizar(1L, request))
                .isInstanceOf(RegraNegocioException.class);

        verify(questaoRepository, never()).findById(any());
    }

    /**
     * Regressão: a atualização precisa reaproveitar a MESMA instância da coleção de
     * alternativas (clear+addAll), nunca substituí-la por uma nova lista — só assim o
     * orphanRemoval do Hibernate remove de fato as alternativas antigas no banco.
     */
    @Test
    void deveSubstituirAlternativasMantendoAMesmaColecaoGerenciadaPeloHibernate() {
        Alternativa antiga = Alternativa.builder().id(10L).letra(LetraAlternativa.A).descricao("antiga").correta(true).build();
        List<Alternativa> colecaoOriginal = new ArrayList<>(List.of(antiga));
        Questao existente = Questao.builder().id(1L).enunciado("Enunciado").tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .alternativas(colecaoOriginal).build();

        AlternativaRequestDTO novaAltRequest = AlternativaRequestDTO.builder()
                .letra(LetraAlternativa.B).descricao("nova").correta(true).build();
        QuestaoRequestDTO request = montarRequestObjetiva(List.of(novaAltRequest));

        Disciplina disciplina = Disciplina.builder().id(1L).build();
        Assunto assunto = Assunto.builder().id(2L).build();
        Alternativa novaAlternativaConvertida = Alternativa.builder().letra(LetraAlternativa.B).descricao("nova").correta(true).build();

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(assuntoRepository.findById(2L)).thenReturn(Optional.of(assunto));
        when(alternativaMapper.toEntityList(request.getAlternativas()))
                .thenReturn(new ArrayList<>(List.of(novaAlternativaConvertida)));
        when(questaoMapper.toResponse(existente)).thenReturn(QuestaoResponseDTO.builder().id(1L).build());

        questaoService.atualizar(1L, request);

        assertThat(existente.getAlternativas()).isSameAs(colecaoOriginal);
        assertThat(existente.getAlternativas()).containsExactly(novaAlternativaConvertida);
        assertThat(novaAlternativaConvertida.getQuestao()).isEqualTo(existente);
    }
}
