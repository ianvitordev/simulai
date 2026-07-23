package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.GerarSimuladoRequestDTO;
import SimulaI.dto.RespostaUsuarioRequestDTO;
import SimulaI.dto.RespostaUsuarioResponseDTO;
import SimulaI.dto.SimuladoResponseDTO;
import SimulaI.dto.SimuladoResultadoDTO;
import SimulaI.entity.Alternativa;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Questao;
import SimulaI.entity.RespostaUsuario;
import SimulaI.entity.Simulado;
import SimulaI.entity.Usuario;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.LetraAlternativa;
import SimulaI.enums.StatusSimulado;
import SimulaI.enums.TipoQuestao;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.RespostaUsuarioMapper;
import SimulaI.mapper.SimuladoMapper;
import SimulaI.repository.ConcursoRepository;
import SimulaI.repository.QuestaoRepository;
import SimulaI.repository.RespostaUsuarioRepository;
import SimulaI.repository.SimuladoRepository;
import SimulaI.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class SimuladoServiceImplTest {

    @Mock
    private SimuladoRepository simuladoRepository;

    @Mock
    private RespostaUsuarioRepository respostaUsuarioRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private ConcursoRepository concursoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SimuladoMapper simuladoMapper;

    @Mock
    private RespostaUsuarioMapper respostaUsuarioMapper;

    @InjectMocks
    private SimuladoServiceImpl simuladoService;

    private Questao questaoValida(Long id) {
        return Questao.builder().id(id).tipo(TipoQuestao.MULTIPLA_ESCOLHA).dificuldade(Dificuldade.FACIL)
                .ativa(true).explicacao("Explicação").build();
    }

    // ---------- gerar ----------

    @Test
    void deveGerarSimuladoSemConcursoSorteandoDoBancoTodo() {
        Usuario usuario = Usuario.builder().id(1L).build();
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(2).tempoLimiteMinutos(60).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findAll()).thenReturn(new ArrayList<>(List.of(questaoValida(1L), questaoValida(2L), questaoValida(3L))));
        when(simuladoRepository.save(any(Simulado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(simuladoMapper.toResponse(any(Simulado.class))).thenReturn(SimuladoResponseDTO.builder().id(1L).build());

        simuladoService.gerar(1L, request);

        ArgumentCaptor<Simulado> captor = ArgumentCaptor.forClass(Simulado.class);
        verify(simuladoRepository).save(captor.capture());
        Simulado salvo = captor.getValue();

        assertThat(salvo.getUsuario()).isEqualTo(usuario);
        assertThat(salvo.getConcurso()).isNull();
        assertThat(salvo.getQuantidadeQuestoes()).isEqualTo(2);
        assertThat(salvo.getQuestoes()).hasSize(2);
        assertThat(salvo.getStatus()).isEqualTo(StatusSimulado.CRIADO);
    }

    @Test
    void deveGerarSimuladoComConcursoFiltrandoPorDisciplinasDoEdital() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Disciplina disciplina = Disciplina.builder().id(5L).build();
        Concurso concurso = Concurso.builder().id(2L).disciplinas(List.of(disciplina)).build();
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().concursoId(2L).quantidadeQuestoes(1).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(concursoRepository.findById(2L)).thenReturn(Optional.of(concurso));
        when(questaoRepository.findByDisciplinaIn(concurso.getDisciplinas())).thenReturn(new ArrayList<>(List.of(questaoValida(1L))));
        when(simuladoRepository.save(any(Simulado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(simuladoMapper.toResponse(any(Simulado.class))).thenReturn(SimuladoResponseDTO.builder().id(1L).build());

        simuladoService.gerar(1L, request);

        verify(questaoRepository).findByDisciplinaIn(concurso.getDisciplinas());
        verify(questaoRepository, never()).findAll();
    }

    @Test
    void deveLancarExcecaoQuandoBancoDeQuestoesInsuficiente() {
        Usuario usuario = Usuario.builder().id(1L).build();
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(5).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findAll()).thenReturn(new ArrayList<>(List.of(questaoValida(1L))));

        assertThatThrownBy(() -> simuladoService.gerar(1L, request)).isInstanceOf(RegraNegocioException.class);
        verify(simuladoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoConcursoSemDisciplinas() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Concurso concurso = Concurso.builder().id(2L).disciplinas(new ArrayList<>()).build();
        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().concursoId(2L).quantidadeQuestoes(1).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(concursoRepository.findById(2L)).thenReturn(Optional.of(concurso));

        assertThatThrownBy(() -> simuladoService.gerar(1L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveFiltrarQuestoesInativasDiscursivasEDificuldadeDiferenteAoGerar() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Questao valida1 = questaoValida(1L);
        Questao valida2 = questaoValida(2L);
        Questao inativa = Questao.builder().id(3L).tipo(TipoQuestao.MULTIPLA_ESCOLHA).dificuldade(Dificuldade.FACIL).ativa(false).build();
        Questao discursiva = Questao.builder().id(4L).tipo(TipoQuestao.DISCURSIVA).dificuldade(Dificuldade.FACIL).ativa(true).build();
        Questao outraDificuldade = Questao.builder().id(5L).tipo(TipoQuestao.MULTIPLA_ESCOLHA).dificuldade(Dificuldade.DIFICIL).ativa(true).build();

        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(2).dificuldade(Dificuldade.FACIL).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(valida1, valida2, inativa, discursiva, outraDificuldade)));
        when(simuladoRepository.save(any(Simulado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(simuladoMapper.toResponse(any(Simulado.class))).thenReturn(SimuladoResponseDTO.builder().id(1L).build());

        simuladoService.gerar(1L, request);

        ArgumentCaptor<Simulado> captor = ArgumentCaptor.forClass(Simulado.class);
        verify(simuladoRepository).save(captor.capture());
        assertThat(captor.getValue().getQuestoes()).containsExactlyInAnyOrder(valida1, valida2);
    }

    /**
     * Regressão: questões geradas antes da exigência de explicação obrigatória (ou que
     * não puderam ser corrigidas por já estarem vinculadas a respostas de simulados
     * finalizados) nunca devem ser sorteadas — a revisão precisa explicar toda questão,
     * sem exceção.
     */
    @Test
    void deveExcluirQuestoesSemExplicacaoAoGerar() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Questao valida = questaoValida(1L);
        Questao semExplicacao = Questao.builder().id(2L).tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .dificuldade(Dificuldade.FACIL).ativa(true).explicacao("").build();
        Questao explicacaoNula = Questao.builder().id(3L).tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .dificuldade(Dificuldade.FACIL).ativa(true).explicacao(null).build();

        GerarSimuladoRequestDTO request = GerarSimuladoRequestDTO.builder().quantidadeQuestoes(1).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findAll())
                .thenReturn(new ArrayList<>(List.of(valida, semExplicacao, explicacaoNula)));
        when(simuladoRepository.save(any(Simulado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(simuladoMapper.toResponse(any(Simulado.class))).thenReturn(SimuladoResponseDTO.builder().id(1L).build());

        simuladoService.gerar(1L, request);

        ArgumentCaptor<Simulado> captor = ArgumentCaptor.forClass(Simulado.class);
        verify(simuladoRepository).save(captor.capture());
        assertThat(captor.getValue().getQuestoes()).containsExactly(valida);
    }

    // ---------- iniciar ----------

    @Test
    void deveIniciarSimuladoCriado() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.CRIADO).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(simuladoMapper.toResponse(simulado)).thenReturn(SimuladoResponseDTO.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).build());

        simuladoService.iniciar(1L);

        assertThat(simulado.getStatus()).isEqualTo(StatusSimulado.EM_ANDAMENTO);
        assertThat(simulado.getInicio()).isNotNull();
    }

    @Test
    void deveLancarExcecaoAoIniciarSimuladoQueNaoEstaCriado() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));

        assertThatThrownBy(() -> simuladoService.iniciar(1L)).isInstanceOf(RegraNegocioException.class);
    }

    // ---------- responderQuestao ----------

    @Test
    void deveResponderQuestaoCriandoNovaResposta() {
        Alternativa altCorreta = Alternativa.builder().id(100L).letra(LetraAlternativa.A).correta(true).build();
        Alternativa altErrada = Alternativa.builder().id(101L).letra(LetraAlternativa.B).correta(false).build();
        Questao questao = Questao.builder().id(10L).tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .alternativas(List.of(altCorreta, altErrada)).build();
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).questoes(List.of(questao)).build();

        RespostaUsuarioRequestDTO request = RespostaUsuarioRequestDTO.builder()
                .questaoId(10L).alternativaMarcadaId(100L).tempoRespostaSegundos(30).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(questaoRepository.findById(10L)).thenReturn(Optional.of(questao));
        when(respostaUsuarioRepository.findBySimuladoAndQuestao(simulado, questao)).thenReturn(Optional.empty());
        when(respostaUsuarioRepository.save(any(RespostaUsuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(respostaUsuarioMapper.toResponse(any(RespostaUsuario.class)))
                .thenReturn(RespostaUsuarioResponseDTO.builder().acertou(true).build());

        RespostaUsuarioResponseDTO resultado = simuladoService.responderQuestao(1L, request);

        assertThat(resultado.getAcertou()).isTrue();

        ArgumentCaptor<RespostaUsuario> captor = ArgumentCaptor.forClass(RespostaUsuario.class);
        verify(respostaUsuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getAlternativaMarcada()).isEqualTo(altCorreta);
        assertThat(captor.getValue().getAcertou()).isTrue();
        assertThat(captor.getValue().getTempoRespostaSegundos()).isEqualTo(30);
    }

    @Test
    void deveAtualizarRespostaExistenteAoResponderNovamente() {
        Alternativa altCorreta = Alternativa.builder().id(100L).letra(LetraAlternativa.A).correta(true).build();
        Alternativa altErrada = Alternativa.builder().id(101L).letra(LetraAlternativa.B).correta(false).build();
        Questao questao = Questao.builder().id(10L).tipo(TipoQuestao.MULTIPLA_ESCOLHA)
                .alternativas(List.of(altCorreta, altErrada)).build();
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).questoes(List.of(questao)).build();
        RespostaUsuario existente = RespostaUsuario.builder().id(500L).simulado(simulado).questao(questao)
                .alternativaMarcada(altErrada).acertou(false).tempoRespostaSegundos(10).build();

        RespostaUsuarioRequestDTO request = RespostaUsuarioRequestDTO.builder()
                .questaoId(10L).alternativaMarcadaId(100L).tempoRespostaSegundos(45).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(questaoRepository.findById(10L)).thenReturn(Optional.of(questao));
        when(respostaUsuarioRepository.findBySimuladoAndQuestao(simulado, questao)).thenReturn(Optional.of(existente));
        when(respostaUsuarioRepository.save(existente)).thenReturn(existente);
        when(respostaUsuarioMapper.toResponse(existente)).thenReturn(RespostaUsuarioResponseDTO.builder().id(500L).acertou(true).build());

        simuladoService.responderQuestao(1L, request);

        assertThat(existente.getAlternativaMarcada()).isEqualTo(altCorreta);
        assertThat(existente.getAcertou()).isTrue();
        assertThat(existente.getTempoRespostaSegundos()).isEqualTo(45);
        verify(respostaUsuarioRepository).save(existente);
    }

    @Test
    void deveLancarExcecaoAoResponderQuestaoQueNaoPertenceAoSimulado() {
        Questao questaoDoSimulado = Questao.builder().id(10L).tipo(TipoQuestao.MULTIPLA_ESCOLHA).alternativas(List.of()).build();
        Questao outraQuestao = Questao.builder().id(20L).tipo(TipoQuestao.MULTIPLA_ESCOLHA).alternativas(List.of()).build();
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).questoes(List.of(questaoDoSimulado)).build();

        RespostaUsuarioRequestDTO request = RespostaUsuarioRequestDTO.builder().questaoId(20L).alternativaMarcadaId(1L).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(questaoRepository.findById(20L)).thenReturn(Optional.of(outraQuestao));

        assertThatThrownBy(() -> simuladoService.responderQuestao(1L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoAoResponderComAlternativaQueNaoPertenceAQuestao() {
        Alternativa altDaQuestao = Alternativa.builder().id(100L).correta(true).build();
        Questao questao = Questao.builder().id(10L).tipo(TipoQuestao.MULTIPLA_ESCOLHA).alternativas(List.of(altDaQuestao)).build();
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).questoes(List.of(questao)).build();

        RespostaUsuarioRequestDTO request = RespostaUsuarioRequestDTO.builder().questaoId(10L).alternativaMarcadaId(999L).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(questaoRepository.findById(10L)).thenReturn(Optional.of(questao));

        assertThatThrownBy(() -> simuladoService.responderQuestao(1L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoAoResponderSimuladoQueNaoEstaEmAndamento() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.CRIADO).questoes(List.of()).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));

        RespostaUsuarioRequestDTO request = RespostaUsuarioRequestDTO.builder().questaoId(1L).alternativaMarcadaId(1L).build();

        assertThatThrownBy(() -> simuladoService.responderQuestao(1L, request)).isInstanceOf(RegraNegocioException.class);
        verify(questaoRepository, never()).findById(any());
    }

    // ---------- finalizar ----------

    @Test
    void deveFinalizarCalculandoResultado() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).quantidadeQuestoes(2).build();
        RespostaUsuario r1 = RespostaUsuario.builder().acertou(true).tempoRespostaSegundos(30).build();
        RespostaUsuario r2 = RespostaUsuario.builder().acertou(false).tempoRespostaSegundos(40).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(respostaUsuarioRepository.findBySimulado(simulado)).thenReturn(List.of(r1, r2));

        SimuladoResultadoDTO resultado = simuladoService.finalizar(1L);

        assertThat(resultado.getTotalQuestoes()).isEqualTo(2);
        assertThat(resultado.getAcertos()).isEqualTo(1);
        assertThat(resultado.getErros()).isEqualTo(1);
        assertThat(resultado.getPercentualAcerto()).isEqualTo(50.0);
        assertThat(resultado.getTempoTotalSegundos()).isEqualTo(70);
        assertThat(simulado.getStatus()).isEqualTo(StatusSimulado.FINALIZADO);
        assertThat(simulado.getFim()).isNotNull();
    }

    @Test
    void deveLancarExcecaoAoFinalizarSimuladoQueNaoEstaEmAndamento() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.CRIADO).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));

        assertThatThrownBy(() -> simuladoService.finalizar(1L)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoAoFinalizarComQuestoesNaoRespondidas() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).quantidadeQuestoes(3).build();
        RespostaUsuario r1 = RespostaUsuario.builder().acertou(true).tempoRespostaSegundos(30).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(respostaUsuarioRepository.findBySimulado(simulado)).thenReturn(List.of(r1));

        assertThatThrownBy(() -> simuladoService.finalizar(1L)).isInstanceOf(RegraNegocioException.class);
        assertThat(simulado.getStatus()).isEqualTo(StatusSimulado.EM_ANDAMENTO);
    }

    // ---------- cancelar ----------

    @Test
    void deveCancelarSimuladoNaoFinalizado() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(simuladoMapper.toResponse(simulado)).thenReturn(SimuladoResponseDTO.builder().id(1L).status(StatusSimulado.CANCELADO).build());

        simuladoService.cancelar(1L);

        assertThat(simulado.getStatus()).isEqualTo(StatusSimulado.CANCELADO);
    }

    @Test
    void deveLancarExcecaoAoCancelarSimuladoFinalizado() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.FINALIZADO).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));

        assertThatThrownBy(() -> simuladoService.cancelar(1L)).isInstanceOf(RegraNegocioException.class);
    }

    // ---------- revisar ----------

    @Test
    void deveRevisarSimuladoFinalizadoRevelandoGabaritoECorretude() {
        Alternativa correta = Alternativa.builder().id(100L).letra(LetraAlternativa.A).descricao("Certo").correta(true).build();
        Alternativa errada = Alternativa.builder().id(101L).letra(LetraAlternativa.B).descricao("Errado").correta(false).build();
        Questao questao = Questao.builder().id(10L).enunciado("Enunciado").explicacao("Explicação")
                .alternativas(List.of(correta, errada)).build();
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.FINALIZADO).questoes(List.of(questao)).build();

        RespostaUsuario resposta = RespostaUsuario.builder().questao(questao).alternativaMarcada(errada).acertou(false).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(respostaUsuarioRepository.findBySimulado(simulado)).thenReturn(List.of(resposta));

        var revisao = simuladoService.revisar(1L);

        assertThat(revisao.getSimuladoId()).isEqualTo(1L);
        assertThat(revisao.getQuestoes()).hasSize(1);

        var questaoRevisao = revisao.getQuestoes().get(0);
        assertThat(questaoRevisao.getRespondida()).isTrue();
        assertThat(questaoRevisao.getAcertou()).isFalse();
        assertThat(questaoRevisao.getAlternativas()).hasSize(2);
        assertThat(questaoRevisao.getAlternativas())
                .filteredOn(alt -> alt.getId().equals(100L))
                .allSatisfy(alt -> {
                    assertThat(alt.getCorreta()).isTrue();
                    assertThat(alt.getMarcadaPeloUsuario()).isFalse();
                });
        assertThat(questaoRevisao.getAlternativas())
                .filteredOn(alt -> alt.getId().equals(101L))
                .allSatisfy(alt -> {
                    assertThat(alt.getCorreta()).isFalse();
                    assertThat(alt.getMarcadaPeloUsuario()).isTrue();
                });
    }

    @Test
    void deveMarcarQuestaoComoNaoRespondidaQuandoUsuarioPulou() {
        Alternativa correta = Alternativa.builder().id(100L).letra(LetraAlternativa.A).correta(true).build();
        Questao questao = Questao.builder().id(10L).enunciado("Enunciado").alternativas(List.of(correta)).build();
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.FINALIZADO).questoes(List.of(questao)).build();

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(respostaUsuarioRepository.findBySimulado(simulado)).thenReturn(List.of());

        var revisao = simuladoService.revisar(1L);

        var questaoRevisao = revisao.getQuestoes().get(0);
        assertThat(questaoRevisao.getRespondida()).isFalse();
        assertThat(questaoRevisao.getAcertou()).isFalse();
    }

    @Test
    void deveLancarExcecaoAoRevisarSimuladoNaoFinalizado() {
        Simulado simulado = Simulado.builder().id(1L).status(StatusSimulado.EM_ANDAMENTO).build();
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));

        assertThatThrownBy(() -> simuladoService.revisar(1L)).isInstanceOf(RegraNegocioException.class);
    }
}
