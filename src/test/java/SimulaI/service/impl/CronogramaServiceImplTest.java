package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.CronogramaResponseDTO;
import SimulaI.dto.EstatisticaAssuntoDTO;
import SimulaI.dto.EstatisticaDisciplinaDTO;
import SimulaI.dto.EstatisticaResponseDTO;
import SimulaI.dto.GerarCronogramaRequestDTO;
import SimulaI.entity.Assunto;
import SimulaI.entity.Cronograma;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Usuario;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.CronogramaMapper;
import SimulaI.repository.AssuntoRepository;
import SimulaI.repository.CronogramaRepository;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.EstatisticaService;
import SimulaI.service.ia.CronogramaGeradoIA;
import SimulaI.service.ia.GeradorCronogramaIA;
import SimulaI.service.ia.ItemCronogramaGeradoIA;
import SimulaI.service.ia.OpcaoEstudoIA;

@ExtendWith(MockitoExtension.class)
class CronogramaServiceImplTest {

    @Mock
    private CronogramaRepository cronogramaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AssuntoRepository assuntoRepository;

    @Mock
    private EstatisticaService estatisticaService;

    @Mock
    private GeradorCronogramaIA geradorCronogramaIA;

    @Mock
    private CronogramaMapper cronogramaMapper;

    @InjectMocks
    private CronogramaServiceImpl cronogramaService;

    private Usuario usuarioTeste() {
        return Usuario.builder().id(1L).nome("Aluno Teste").build();
    }

    private Assunto assuntoTeste(Long id, String disciplinaNome, String assuntoNome) {
        Disciplina disciplina = Disciplina.builder().id(1L).nome(disciplinaNome).build();
        return Assunto.builder().id(id).nome(assuntoNome).disciplina(disciplina).build();
    }

    private EstatisticaResponseDTO estatisticasVazias() {
        return EstatisticaResponseDTO.builder().porDisciplina(List.of()).evolucao(List.of()).build();
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        GerarCronogramaRequestDTO request = GerarCronogramaRequestDTO.builder().diasPorSemana(3).horasPorDia(1).build();

        assertThatThrownBy(() -> cronogramaService.gerar(1L, request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoCatalogoVazio() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioTeste()));
        when(assuntoRepository.findAll()).thenReturn(List.of());

        GerarCronogramaRequestDTO request = GerarCronogramaRequestDTO.builder().diasPorSemana(3).horasPorDia(1).build();

        assertThatThrownBy(() -> cronogramaService.gerar(1L, request))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveGerarCronogramaResolvendoAssuntoPorNomeEApagandoAnterior() {
        Usuario usuario = usuarioTeste();
        Assunto crase = assuntoTeste(1L, "Português", "Crase");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(assuntoRepository.findAll()).thenReturn(List.of(crase));
        when(estatisticaService.obterEstatisticas(1L)).thenReturn(estatisticasVazias());

        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Foque em Crase.",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Português", "Crase", 60, "Revisão", "Prioridade")));
        when(geradorCronogramaIA.gerar(any(), anyInt(), anyInt())).thenReturn(respostaIA);

        Cronograma anterior = Cronograma.builder().id(99L).usuario(usuario).build();
        when(cronogramaRepository.findByUsuario(usuario)).thenReturn(Optional.of(anterior));

        when(cronogramaRepository.save(any(Cronograma.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cronogramaMapper.toResponse(any(Cronograma.class)))
                .thenReturn(CronogramaResponseDTO.builder().id(1L).build());

        GerarCronogramaRequestDTO request = GerarCronogramaRequestDTO.builder().diasPorSemana(3).horasPorDia(1).build();
        CronogramaResponseDTO resultado = cronogramaService.gerar(1L, request);

        assertThat(resultado).isNotNull();
        verify(cronogramaRepository).delete(anterior);

        ArgumentCaptor<Cronograma> cronogramaCaptor = ArgumentCaptor.forClass(Cronograma.class);
        verify(cronogramaRepository).save(cronogramaCaptor.capture());
        Cronograma salvo = cronogramaCaptor.getValue();
        assertThat(salvo.getItens()).hasSize(1);
        assertThat(salvo.getItens().get(0).getAssunto().getNome()).isEqualTo("Crase");
        assertThat(salvo.getItens().get(0).getDisciplina().getNome()).isEqualTo("Português");
        assertThat(salvo.getItens().get(0).getCronograma()).isSameAs(salvo);
    }

    @Test
    void naoDeveApagarNadaQuandoNaoExisteCronogramaAnterior() {
        Usuario usuario = usuarioTeste();
        Assunto crase = assuntoTeste(1L, "Português", "Crase");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(assuntoRepository.findAll()).thenReturn(List.of(crase));
        when(estatisticaService.obterEstatisticas(1L)).thenReturn(estatisticasVazias());

        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA(
                "Observação",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Português", "Crase", 60, "Revisão", "Prioridade")));
        when(geradorCronogramaIA.gerar(any(), anyInt(), anyInt())).thenReturn(respostaIA);

        when(cronogramaRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
        when(cronogramaRepository.save(any(Cronograma.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cronogramaMapper.toResponse(any(Cronograma.class)))
                .thenReturn(CronogramaResponseDTO.builder().id(1L).build());

        GerarCronogramaRequestDTO request = GerarCronogramaRequestDTO.builder().diasPorSemana(3).horasPorDia(1).build();
        cronogramaService.gerar(1L, request);

        verify(cronogramaRepository, never()).delete(any());
    }

    @Test
    void deveRepassarDesempenhoRealParaAsOpcoesDaIA() {
        Usuario usuario = usuarioTeste();
        Assunto crase = assuntoTeste(1L, "Português", "Crase");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(assuntoRepository.findAll()).thenReturn(List.of(crase));

        EstatisticaAssuntoDTO assuntoStats = EstatisticaAssuntoDTO.builder()
                .assunto("Crase").totalRespondidas(10).acertos(4).percentual(40.0).build();
        EstatisticaDisciplinaDTO disciplinaStats = EstatisticaDisciplinaDTO.builder()
                .disciplina("Português").porAssunto(List.of(assuntoStats)).build();
        when(estatisticaService.obterEstatisticas(1L)).thenReturn(
                EstatisticaResponseDTO.builder().porDisciplina(List.of(disciplinaStats)).evolucao(List.of()).build());

        CronogramaGeradoIA respostaIA = new CronogramaGeradoIA("Observação",
                List.of(new ItemCronogramaGeradoIA("SEGUNDA", "Português", "Crase", 60, "Revisão", "Prioridade")));
        when(geradorCronogramaIA.gerar(any(), anyInt(), anyInt())).thenReturn(respostaIA);
        when(cronogramaRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
        when(cronogramaRepository.save(any(Cronograma.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cronogramaMapper.toResponse(any(Cronograma.class)))
                .thenReturn(CronogramaResponseDTO.builder().id(1L).build());

        GerarCronogramaRequestDTO request = GerarCronogramaRequestDTO.builder().diasPorSemana(3).horasPorDia(1).build();
        cronogramaService.gerar(1L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OpcaoEstudoIA>> opcoesCaptor = ArgumentCaptor.forClass(List.class);
        verify(geradorCronogramaIA).gerar(opcoesCaptor.capture(), anyInt(), anyInt());

        OpcaoEstudoIA opcao = opcoesCaptor.getValue().get(0);
        assertThat(opcao.percentualAcerto()).isEqualTo(40.0);
        assertThat(opcao.totalRespondidas()).isEqualTo(10);
    }

    @Test
    void obterAtualDeveLancarExcecaoQuandoNuncaGerouCronograma() {
        Usuario usuario = usuarioTeste();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cronogramaRepository.findByUsuario(usuario)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cronogramaService.obterAtual(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void obterAtualDeveRetornarCronogramaExistente() {
        Usuario usuario = usuarioTeste();
        Cronograma cronograma = Cronograma.builder().id(5L).usuario(usuario).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cronogramaRepository.findByUsuario(usuario)).thenReturn(Optional.of(cronograma));
        when(cronogramaMapper.toResponse(cronograma)).thenReturn(CronogramaResponseDTO.builder().id(5L).build());

        CronogramaResponseDTO resultado = cronogramaService.obterAtual(1L);

        assertThat(resultado.getId()).isEqualTo(5L);
    }
}
