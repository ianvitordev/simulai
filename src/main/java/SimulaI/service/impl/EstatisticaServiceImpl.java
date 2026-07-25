package SimulaI.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.EstatisticaAssuntoDTO;
import SimulaI.dto.EstatisticaDisciplinaDTO;
import SimulaI.dto.EstatisticaEvolucaoDTO;
import SimulaI.dto.EstatisticaResponseDTO;
import SimulaI.entity.RespostaUsuario;
import SimulaI.entity.Simulado;
import SimulaI.enums.StatusSimulado;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.repository.RespostaUsuarioRepository;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.EstatisticaService;

/**
 * Agrega, em Java, as respostas do usuário através de todos os seus simulados (exceto os
 * CANCELADO) por disciplina/assunto e por simulado (evolução no tempo). Não existia
 * nenhuma query/serviço multi-simulado antes disso — o único "resumo" que já existia era
 * por simulado individual (SimuladoServiceImpl.finalizar/revisar).
 */
@Service
@Transactional(readOnly = true)
public class EstatisticaServiceImpl implements EstatisticaService {

    private final RespostaUsuarioRepository respostaUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public EstatisticaServiceImpl(RespostaUsuarioRepository respostaUsuarioRepository,
                                   UsuarioRepository usuarioRepository) {
        this.respostaUsuarioRepository = respostaUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public EstatisticaResponseDTO obterEstatisticas(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw RecursoNaoEncontradoException.porId("Usuário", usuarioId);
        }

        List<RespostaUsuario> respostas = respostaUsuarioRepository.findParaEstatisticas(usuarioId).stream()
                .filter(resposta -> resposta.getSimulado().getStatus() != StatusSimulado.CANCELADO)
                .toList();

        int totalRespondidas = respostas.size();
        int totalAcertos = contarAcertos(respostas);
        double percentualGeral = calcularPercentual(totalAcertos, totalRespondidas);
        long tempoTotalSegundos = respostas.stream().mapToLong(RespostaUsuario::getTempoRespostaSegundos).sum();

        List<Simulado> simuladosFinalizados = respostas.stream()
                .map(RespostaUsuario::getSimulado)
                .filter(simulado -> simulado.getStatus() == StatusSimulado.FINALIZADO)
                .distinct()
                .toList();

        return EstatisticaResponseDTO.builder()
                .totalRespondidas(totalRespondidas)
                .totalAcertos(totalAcertos)
                .percentualGeral(percentualGeral)
                .totalSimuladosFinalizados(simuladosFinalizados.size())
                .tempoTotalSegundos(tempoTotalSegundos)
                .porDisciplina(montarPorDisciplina(respostas))
                .evolucao(montarEvolucao(respostas))
                .build();
    }

    private List<EstatisticaDisciplinaDTO> montarPorDisciplina(List<RespostaUsuario> respostas) {
        Map<String, List<RespostaUsuario>> porDisciplina = respostas.stream()
                .collect(Collectors.groupingBy(resposta -> resposta.getQuestao().getDisciplina().getNome()));

        return porDisciplina.entrySet().stream()
                .map(entrada -> {
                    List<RespostaUsuario> respostasDaDisciplina = entrada.getValue();
                    int acertos = contarAcertos(respostasDaDisciplina);

                    return EstatisticaDisciplinaDTO.builder()
                            .disciplina(entrada.getKey())
                            .totalRespondidas(respostasDaDisciplina.size())
                            .acertos(acertos)
                            .percentual(calcularPercentual(acertos, respostasDaDisciplina.size()))
                            .porAssunto(montarPorAssunto(respostasDaDisciplina))
                            .build();
                })
                .sorted(Comparator.comparing(EstatisticaDisciplinaDTO::getDisciplina))
                .toList();
    }

    private List<EstatisticaAssuntoDTO> montarPorAssunto(List<RespostaUsuario> respostasDaDisciplina) {
        Map<String, List<RespostaUsuario>> porAssunto = respostasDaDisciplina.stream()
                .collect(Collectors.groupingBy(resposta -> resposta.getQuestao().getAssunto().getNome()));

        return porAssunto.entrySet().stream()
                .map(entrada -> {
                    List<RespostaUsuario> respostasDoAssunto = entrada.getValue();
                    int acertos = contarAcertos(respostasDoAssunto);

                    return EstatisticaAssuntoDTO.builder()
                            .assunto(entrada.getKey())
                            .totalRespondidas(respostasDoAssunto.size())
                            .acertos(acertos)
                            .percentual(calcularPercentual(acertos, respostasDoAssunto.size()))
                            .build();
                })
                .sorted(Comparator.comparing(EstatisticaAssuntoDTO::getAssunto))
                .toList();
    }

    private List<EstatisticaEvolucaoDTO> montarEvolucao(List<RespostaUsuario> respostas) {
        Map<Simulado, List<RespostaUsuario>> porSimulado = respostas.stream()
                .filter(resposta -> resposta.getSimulado().getStatus() == StatusSimulado.FINALIZADO)
                .collect(Collectors.groupingBy(RespostaUsuario::getSimulado));

        return porSimulado.entrySet().stream()
                .map(entrada -> {
                    Simulado simulado = entrada.getKey();
                    int acertos = contarAcertos(entrada.getValue());

                    return EstatisticaEvolucaoDTO.builder()
                            .simuladoId(simulado.getId())
                            .data(simulado.getFim())
                            .percentual(calcularPercentual(acertos, entrada.getValue().size()))
                            .build();
                })
                .sorted(Comparator.comparing(EstatisticaEvolucaoDTO::getData))
                .toList();
    }

    private int contarAcertos(List<RespostaUsuario> respostas) {
        return (int) respostas.stream().filter(resposta -> Boolean.TRUE.equals(resposta.getAcertou())).count();
    }

    private double calcularPercentual(int acertos, int total) {
        return total == 0 ? 0.0 : (acertos * 100.0) / total;
    }
}
