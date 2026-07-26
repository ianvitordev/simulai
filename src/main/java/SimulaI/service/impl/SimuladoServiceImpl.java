package SimulaI.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.GerarProvaRequestDTO;
import SimulaI.dto.GerarSimuladoRequestDTO;
import SimulaI.dto.RespostaUsuarioRequestDTO;
import SimulaI.dto.RespostaUsuarioResponseDTO;
import SimulaI.dto.RevisaoAlternativaDTO;
import SimulaI.dto.RevisaoQuestaoDTO;
import SimulaI.dto.SimuladoResponseDTO;
import SimulaI.dto.SimuladoResultadoDTO;
import SimulaI.dto.SimuladoRevisaoDTO;
import SimulaI.entity.Alternativa;
import SimulaI.entity.Concurso;
import SimulaI.entity.Questao;
import SimulaI.entity.RespostaUsuario;
import SimulaI.entity.Simulado;
import SimulaI.entity.Usuario;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.StatusSimulado;
import SimulaI.enums.TipoQuestao;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.RespostaUsuarioMapper;
import SimulaI.mapper.SimuladoMapper;
import SimulaI.repository.ConcursoRepository;
import SimulaI.repository.QuestaoRepository;
import SimulaI.repository.RespostaUsuarioRepository;
import SimulaI.repository.SimuladoRepository;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.SimuladoService;

@Service
@Transactional
public class SimuladoServiceImpl implements SimuladoService {

    private final SimuladoRepository simuladoRepository;
    private final RespostaUsuarioRepository respostaUsuarioRepository;
    private final QuestaoRepository questaoRepository;
    private final ConcursoRepository concursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimuladoMapper simuladoMapper;
    private final RespostaUsuarioMapper respostaUsuarioMapper;

    public SimuladoServiceImpl(SimuladoRepository simuladoRepository,
                                RespostaUsuarioRepository respostaUsuarioRepository,
                                QuestaoRepository questaoRepository,
                                ConcursoRepository concursoRepository,
                                UsuarioRepository usuarioRepository,
                                SimuladoMapper simuladoMapper,
                                RespostaUsuarioMapper respostaUsuarioMapper) {
        this.simuladoRepository = simuladoRepository;
        this.respostaUsuarioRepository = respostaUsuarioRepository;
        this.questaoRepository = questaoRepository;
        this.concursoRepository = concursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.simuladoMapper = simuladoMapper;
        this.respostaUsuarioMapper = respostaUsuarioMapper;
    }

    @Override
    public SimuladoResponseDTO gerar(Long usuarioId, GerarSimuladoRequestDTO request) {
        Usuario usuario = buscarUsuario(usuarioId);
        Concurso concurso = request.getConcursoId() != null ? buscarConcurso(request.getConcursoId()) : null;

        List<Questao> candidatas = selecionarCandidatas(concurso, request.getDificuldade());
        int quantidadeSolicitada = request.getQuantidadeQuestoes();

        if (candidatas.size() < quantidadeSolicitada) {
            throw new RegraNegocioException(
                    "Banco de questões insuficiente: %d disponíveis, %d solicitadas."
                            .formatted(candidatas.size(), quantidadeSolicitada));
        }

        Collections.shuffle(candidatas);
        List<Questao> selecionadas = new ArrayList<>(candidatas.subList(0, quantidadeSolicitada));

        Simulado simulado = Simulado.builder()
                .usuario(usuario)
                .concurso(concurso)
                .quantidadeQuestoes(quantidadeSolicitada)
                .tempoLimiteMinutos(request.getTempoLimiteMinutos() != null ? request.getTempoLimiteMinutos() : 0)
                .status(StatusSimulado.CRIADO)
                .questoes(selecionadas)
                .respostas(new ArrayList<>())
                .build();

        Simulado simuladoSalvo = simuladoRepository.save(simulado);
        return simuladoMapper.toResponse(simuladoSalvo);
    }

    @Override
    public SimuladoResponseDTO gerarProva(Long usuarioId, GerarProvaRequestDTO request) {
        Usuario usuario = buscarUsuario(usuarioId);
        Concurso concurso = buscarConcurso(request.getConcursoId());

        List<Questao> questoesDaProva =
                questaoRepository.findByConcursoAndGeradaPorIAFalseAndAtivaTrueOrderById(concurso);

        if (questoesDaProva.isEmpty()) {
            throw new RegraNegocioException("Este concurso não possui uma prova real cadastrada.");
        }

        Simulado simulado = Simulado.builder()
                .usuario(usuario)
                .concurso(concurso)
                .quantidadeQuestoes(questoesDaProva.size())
                .tempoLimiteMinutos(0)
                .status(StatusSimulado.CRIADO)
                .questoes(new ArrayList<>(questoesDaProva))
                .respostas(new ArrayList<>())
                .build();

        Simulado simuladoSalvo = simuladoRepository.save(simulado);
        return simuladoMapper.toResponse(simuladoSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public SimuladoResponseDTO buscarPorId(Long id) {
        return simuladoMapper.toResponse(buscarEntidadePorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimuladoResponseDTO> listarPorUsuario(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return simuladoMapper.toResponseList(simuladoRepository.findByUsuario(usuario));
    }

    @Override
    public SimuladoResponseDTO iniciar(Long simuladoId) {
        Simulado simulado = buscarEntidadePorId(simuladoId);

        if (simulado.getStatus() != StatusSimulado.CRIADO) {
            throw new RegraNegocioException("Só é possível iniciar um simulado com status CRIADO.");
        }

        simulado.setStatus(StatusSimulado.EM_ANDAMENTO);
        simulado.setInicio(LocalDateTime.now());

        return simuladoMapper.toResponse(simulado);
    }

    @Override
    public RespostaUsuarioResponseDTO responderQuestao(Long simuladoId, RespostaUsuarioRequestDTO request) {
        Simulado simulado = buscarEntidadePorId(simuladoId);

        if (simulado.getStatus() != StatusSimulado.EM_ANDAMENTO) {
            throw new RegraNegocioException("Só é possível responder questões de um simulado em andamento.");
        }

        Questao questao = questaoRepository.findById(request.getQuestaoId())
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Questão", request.getQuestaoId()));

        boolean pertenceAoSimulado = simulado.getQuestoes().stream()
                .anyMatch(q -> q.getId().equals(questao.getId()));
        if (!pertenceAoSimulado) {
            throw new RegraNegocioException("Esta questão não faz parte deste simulado.");
        }

        Alternativa alternativaMarcada = questao.getAlternativas().stream()
                .filter(alternativa -> alternativa.getId().equals(request.getAlternativaMarcadaId()))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("A alternativa informada não pertence à questão."));

        RespostaUsuario resposta = respostaUsuarioRepository.findBySimuladoAndQuestao(simulado, questao)
                .orElseGet(() -> RespostaUsuario.builder().simulado(simulado).questao(questao).build());

        resposta.setAlternativaMarcada(alternativaMarcada);
        resposta.setAcertou(alternativaMarcada.getCorreta());
        resposta.setTempoRespostaSegundos(
                request.getTempoRespostaSegundos() != null ? request.getTempoRespostaSegundos() : 0);

        RespostaUsuario respostaSalva = respostaUsuarioRepository.save(resposta);
        return respostaUsuarioMapper.toResponse(respostaSalva);
    }

    @Override
    public SimuladoResultadoDTO finalizar(Long simuladoId) {
        Simulado simulado = buscarEntidadePorId(simuladoId);

        if (simulado.getStatus() != StatusSimulado.EM_ANDAMENTO) {
            throw new RegraNegocioException("Só é possível finalizar um simulado em andamento.");
        }

        List<RespostaUsuario> respostas = respostaUsuarioRepository.findBySimulado(simulado);
        if (respostas.size() < simulado.getQuantidadeQuestoes()) {
            throw new RegraNegocioException("Responda todas as questões antes de finalizar o simulado.");
        }

        simulado.setStatus(StatusSimulado.FINALIZADO);
        simulado.setFim(LocalDateTime.now());

        int totalQuestoes = simulado.getQuantidadeQuestoes();
        int acertos = (int) respostas.stream().filter(r -> Boolean.TRUE.equals(r.getAcertou())).count();
        int tempoTotal = respostas.stream().mapToInt(RespostaUsuario::getTempoRespostaSegundos).sum();
        double percentualAcerto = totalQuestoes == 0 ? 0.0 : (acertos * 100.0) / totalQuestoes;

        return SimuladoResultadoDTO.builder()
                .simuladoId(simulado.getId())
                .totalQuestoes(totalQuestoes)
                .acertos(acertos)
                .erros(totalQuestoes - acertos)
                .percentualAcerto(percentualAcerto)
                .tempoTotalSegundos(tempoTotal)
                .build();
    }

    @Override
    public SimuladoResponseDTO cancelar(Long simuladoId) {
        Simulado simulado = buscarEntidadePorId(simuladoId);

        if (simulado.getStatus() == StatusSimulado.FINALIZADO) {
            throw new RegraNegocioException("Não é possível cancelar um simulado já finalizado.");
        }

        simulado.setStatus(StatusSimulado.CANCELADO);
        return simuladoMapper.toResponse(simulado);
    }

    @Override
    @Transactional(readOnly = true)
    public SimuladoRevisaoDTO revisar(Long simuladoId) {
        Simulado simulado = buscarEntidadePorId(simuladoId);

        if (simulado.getStatus() != StatusSimulado.FINALIZADO) {
            throw new RegraNegocioException("A revisão só fica disponível depois de finalizar o simulado.");
        }

        Map<Long, RespostaUsuario> respostaPorQuestaoId = respostaUsuarioRepository.findBySimulado(simulado).stream()
                .collect(Collectors.toMap(resposta -> resposta.getQuestao().getId(), resposta -> resposta));

        List<RevisaoQuestaoDTO> questoesRevisao = simulado.getQuestoes().stream()
                .map(questao -> montarRevisaoQuestao(questao, respostaPorQuestaoId.get(questao.getId())))
                .toList();

        return SimuladoRevisaoDTO.builder()
                .simuladoId(simulado.getId())
                .questoes(questoesRevisao)
                .build();
    }

    private RevisaoQuestaoDTO montarRevisaoQuestao(Questao questao, RespostaUsuario resposta) {
        Long alternativaMarcadaId = resposta != null && resposta.getAlternativaMarcada() != null
                ? resposta.getAlternativaMarcada().getId()
                : null;

        List<RevisaoAlternativaDTO> alternativas = questao.getAlternativas().stream()
                .map(alternativa -> RevisaoAlternativaDTO.builder()
                        .id(alternativa.getId())
                        .letra(alternativa.getLetra())
                        .descricao(alternativa.getDescricao())
                        .correta(Boolean.TRUE.equals(alternativa.getCorreta()))
                        .marcadaPeloUsuario(alternativa.getId().equals(alternativaMarcadaId))
                        .build())
                .toList();

        return RevisaoQuestaoDTO.builder()
                .questaoId(questao.getId())
                .enunciado(questao.getEnunciado())
                .explicacao(questao.getExplicacao())
                .respondida(resposta != null)
                .acertou(resposta != null && Boolean.TRUE.equals(resposta.getAcertou()))
                .alternativas(alternativas)
                .build();
    }

    /**
     * Restringe o sorteio a questões objetivas: RespostaUsuario.alternativaMarcada é
     * obrigatória no schema, então questões DISCURSIVA (sem alternativas) não podem
     * compor um simulado corrigível automaticamente. Também exclui questões sem
     * explicação preenchida — a revisão pós-simulado precisa explicar toda questão,
     * sem exceção, e algumas questões antigas (geradas antes dessa exigência existir)
     * ainda não puderam ser corrigidas por já estarem vinculadas a respostas de
     * simulados já finalizados.
     */
    private List<Questao> selecionarCandidatas(Concurso concurso, Dificuldade dificuldade) {
        List<Questao> pool;

        if (concurso != null) {
            if (concurso.getDisciplinas() == null || concurso.getDisciplinas().isEmpty()) {
                throw new RegraNegocioException("O concurso informado não possui disciplinas associadas.");
            }
            pool = questaoRepository.findByDisciplinaIn(concurso.getDisciplinas());
        } else {
            pool = questaoRepository.findAll();
        }

        return pool.stream()
                .filter(questao -> Boolean.TRUE.equals(questao.getAtiva()))
                .filter(questao -> questao.getTipo() != TipoQuestao.DISCURSIVA)
                .filter(questao -> questao.getExplicacao() != null && !questao.getExplicacao().isBlank())
                .filter(questao -> dificuldade == null || questao.getDificuldade() == dificuldade)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Simulado buscarEntidadePorId(Long id) {
        return simuladoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Simulado", id));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Usuário", usuarioId));
    }

    private Concurso buscarConcurso(Long concursoId) {
        return concursoRepository.findById(concursoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Concurso", concursoId));
    }
}
