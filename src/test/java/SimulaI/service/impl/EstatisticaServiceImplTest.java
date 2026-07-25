package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.EstatisticaDisciplinaDTO;
import SimulaI.dto.EstatisticaResponseDTO;
import SimulaI.entity.Assunto;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Questao;
import SimulaI.entity.RespostaUsuario;
import SimulaI.entity.Simulado;
import SimulaI.enums.StatusSimulado;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.repository.RespostaUsuarioRepository;
import SimulaI.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class EstatisticaServiceImplTest {

    @Mock
    private RespostaUsuarioRepository respostaUsuarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EstatisticaServiceImpl estatisticaService;

    private Questao questao(Long disciplinaId, String disciplinaNome, Long assuntoId, String assuntoNome) {
        Disciplina disciplina = Disciplina.builder().id(disciplinaId).nome(disciplinaNome).build();
        Assunto assunto = Assunto.builder().id(assuntoId).nome(assuntoNome).disciplina(disciplina).build();
        return Questao.builder().disciplina(disciplina).assunto(assunto).build();
    }

    private Simulado simulado(Long id, StatusSimulado status, LocalDateTime fim) {
        return Simulado.builder().id(id).status(status).fim(fim).build();
    }

    private RespostaUsuario resposta(Simulado simulado, Questao questao, boolean acertou, int tempoSegundos) {
        return RespostaUsuario.builder().simulado(simulado).questao(questao).acertou(acertou)
                .tempoRespostaSegundos(tempoSegundos).build();
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> estatisticaService.obterEstatisticas(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveRetornarTudoZeradoQuandoUsuarioNuncaRespondeuNada() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(respostaUsuarioRepository.findParaEstatisticas(1L)).thenReturn(List.of());

        EstatisticaResponseDTO resultado = estatisticaService.obterEstatisticas(1L);

        assertThat(resultado.getTotalRespondidas()).isZero();
        assertThat(resultado.getTotalAcertos()).isZero();
        assertThat(resultado.getPercentualGeral()).isZero();
        assertThat(resultado.getTotalSimuladosFinalizados()).isZero();
        assertThat(resultado.getPorDisciplina()).isEmpty();
        assertThat(resultado.getEvolucao()).isEmpty();
    }

    @Test
    void deveExcluirRespostasDeSimuladoCancelado() {
        Simulado cancelado = simulado(1L, StatusSimulado.CANCELADO, null);
        Questao questao = questao(1L, "Português", 1L, "Crase");

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(respostaUsuarioRepository.findParaEstatisticas(1L))
                .thenReturn(List.of(resposta(cancelado, questao, true, 30)));

        EstatisticaResponseDTO resultado = estatisticaService.obterEstatisticas(1L);

        assertThat(resultado.getTotalRespondidas()).isZero();
        assertThat(resultado.getPorDisciplina()).isEmpty();
    }

    @Test
    void deveAgruparPorDisciplinaEAssuntoCalculandoPercentuais() {
        Simulado simulado = simulado(1L, StatusSimulado.EM_ANDAMENTO, null);
        Questao crase = questao(1L, "Português", 1L, "Crase");
        Questao concordancia = questao(1L, "Português", 2L, "Concordância");

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(respostaUsuarioRepository.findParaEstatisticas(1L)).thenReturn(List.of(
                resposta(simulado, crase, true, 10),
                resposta(simulado, crase, false, 20),
                resposta(simulado, concordancia, true, 15)));

        EstatisticaResponseDTO resultado = estatisticaService.obterEstatisticas(1L);

        assertThat(resultado.getTotalRespondidas()).isEqualTo(3);
        assertThat(resultado.getTotalAcertos()).isEqualTo(2);
        assertThat(resultado.getPercentualGeral()).isEqualTo(200.0 / 3.0);
        assertThat(resultado.getTempoTotalSegundos()).isEqualTo(45L);

        assertThat(resultado.getPorDisciplina()).hasSize(1);
        EstatisticaDisciplinaDTO portugues = resultado.getPorDisciplina().get(0);
        assertThat(portugues.getDisciplina()).isEqualTo("Português");
        assertThat(portugues.getTotalRespondidas()).isEqualTo(3);
        assertThat(portugues.getAcertos()).isEqualTo(2);

        assertThat(portugues.getPorAssunto()).hasSize(2);
        assertThat(portugues.getPorAssunto()).anySatisfy(assunto -> {
            if (assunto.getAssunto().equals("Crase")) {
                assertThat(assunto.getTotalRespondidas()).isEqualTo(2);
                assertThat(assunto.getAcertos()).isEqualTo(1);
                assertThat(assunto.getPercentual()).isEqualTo(50.0);
            }
        });
    }

    @Test
    void deveMontarEvolucaoSoComSimuladosFinalizadosOrdenadaPorData() {
        Simulado simulado1 = simulado(1L, StatusSimulado.FINALIZADO, LocalDateTime.of(2026, 1, 10, 10, 0));
        Simulado simulado2 = simulado(2L, StatusSimulado.FINALIZADO, LocalDateTime.of(2026, 1, 5, 10, 0));
        Simulado emAndamento = simulado(3L, StatusSimulado.EM_ANDAMENTO, null);
        Questao questao = questao(1L, "Matemática", 1L, "Porcentagem");

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(respostaUsuarioRepository.findParaEstatisticas(1L)).thenReturn(List.of(
                resposta(simulado1, questao, true, 10),
                resposta(simulado2, questao, false, 10),
                resposta(emAndamento, questao, true, 10)));

        EstatisticaResponseDTO resultado = estatisticaService.obterEstatisticas(1L);

        assertThat(resultado.getTotalSimuladosFinalizados()).isEqualTo(2);
        assertThat(resultado.getEvolucao()).hasSize(2);
        // ordenado por data: simulado2 (05/jan) vem antes do simulado1 (10/jan)
        assertThat(resultado.getEvolucao().get(0).getSimuladoId()).isEqualTo(2L);
        assertThat(resultado.getEvolucao().get(1).getSimuladoId()).isEqualTo(1L);
    }
}
