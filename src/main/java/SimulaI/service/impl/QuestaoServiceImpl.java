package SimulaI.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import SimulaI.dto.AlternativaRequestDTO;
import SimulaI.dto.GerarQuestaoIARequestDTO;
import SimulaI.dto.QuestaoAdminResponseDTO;
import SimulaI.dto.QuestaoRequestDTO;
import SimulaI.dto.QuestaoResponseDTO;
import SimulaI.entity.Alternativa;
import SimulaI.entity.Assunto;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Questao;
import SimulaI.entity.Usuario;
import SimulaI.enums.Dificuldade;
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
import SimulaI.service.QuestaoService;
import SimulaI.service.ia.AlternativaGeradaIA;
import SimulaI.service.ia.GeradorQuestaoIA;
import SimulaI.service.ia.IndexadorConteudoIA;
import SimulaI.service.ia.QuestaoGeradaIA;

@Slf4j
@Service
@Transactional
public class QuestaoServiceImpl implements QuestaoService {

    private static final String FONTE_GERADA_POR_IA = "Gerado por IA";

    /**
     * Limite de enunciados existentes enviados no prompt anti-repetição: grande o
     * suficiente para dar contexto real, sem inflar o prompt indefinidamente conforme o
     * banco de questões cresce.
     */
    private static final int LIMITE_ENUNCIADOS_ANTI_REPETICAO = 20;

    private final QuestaoRepository questaoRepository;
    private final ConcursoRepository concursoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final UsuarioRepository usuarioRepository;
    private final QuestaoMapper questaoMapper;
    private final AlternativaMapper alternativaMapper;
    private final GeradorQuestaoIA geradorQuestaoIA;
    private final IndexadorConteudoIA indexadorConteudoIA;

    public QuestaoServiceImpl(QuestaoRepository questaoRepository,
                               ConcursoRepository concursoRepository,
                               DisciplinaRepository disciplinaRepository,
                               AssuntoRepository assuntoRepository,
                               UsuarioRepository usuarioRepository,
                               QuestaoMapper questaoMapper,
                               AlternativaMapper alternativaMapper,
                               GeradorQuestaoIA geradorQuestaoIA,
                               IndexadorConteudoIA indexadorConteudoIA) {
        this.questaoRepository = questaoRepository;
        this.concursoRepository = concursoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.usuarioRepository = usuarioRepository;
        this.questaoMapper = questaoMapper;
        this.alternativaMapper = alternativaMapper;
        this.geradorQuestaoIA = geradorQuestaoIA;
        this.indexadorConteudoIA = indexadorConteudoIA;
    }

    @Override
    public QuestaoResponseDTO cadastrar(QuestaoRequestDTO request) {
        validarAlternativas(request.getTipo(), request.getAlternativas());

        Questao questao = questaoMapper.toEntity(request);
        questao.setDisciplina(buscarDisciplina(request.getDisciplinaId()));
        questao.setAssunto(buscarAssunto(request.getAssuntoId()));
        questao.setConcurso(buscarConcursoOpcional(request.getConcursoId()));
        questao.setUsuario(buscarUsuarioOpcional(request.getUsuarioId()));

        Questao questaoSalva = questaoRepository.save(questao);
        indexarParaRAGSemFalhar(questaoSalva);

        return questaoMapper.toResponse(questaoSalva);
    }

    /**
     * Indexação é um reforço de qualidade para futuras gerações (RAG), não um requisito
     * de persistência — uma falha aqui (ex.: vector store indisponível) nunca deve
     * impedir a criação da questão em si.
     */
    private void indexarParaRAGSemFalhar(Questao questao) {
        try {
            indexadorConteudoIA.indexarQuestao(questao);
        } catch (Exception e) {
            log.warn("Falha ao indexar questão {} para RAG: {}", questao.getId(), e.getMessage());
        }
    }

    @Override
    public List<QuestaoResponseDTO> gerarViaIA(GerarQuestaoIARequestDTO request) {
        Disciplina disciplina = buscarDisciplina(request.getDisciplinaId());
        Assunto assunto = buscarAssunto(request.getAssuntoId());
        Concurso concurso = buscarConcursoOpcional(request.getConcursoId());
        Long editalConcursoId = resolverConcursoParaEdital(concurso);

        // Consulta direta ao banco (não depende de embeddings/vector store): pega as
        // questões mais recentes desse assunto pra instruir a IA a não repetir. A lista
        // cresce a cada iteração do próprio lote, então a 2ª questão gerada já "enxerga"
        // a 1ª, e assim por diante — sem isso, pedir quantidade > 1 de uma vez tendia a
        // gerar quase-duplicatas entre si.
        List<String> enunciadosExistentes = questaoRepository.findByAssuntoOrderByIdDesc(assunto).stream()
                .limit(LIMITE_ENUNCIADOS_ANTI_REPETICAO)
                .map(Questao::getEnunciado)
                .collect(Collectors.toCollection(ArrayList::new));

        List<QuestaoResponseDTO> geradas = new ArrayList<>();
        for (int i = 0; i < request.getQuantidade(); i++) {
            QuestaoGeradaIA questaoGerada = geradorQuestaoIA.gerar(disciplina, assunto, request.getDificuldade(),
                    request.getTipo(), concurso, editalConcursoId, enunciadosExistentes);
            enunciadosExistentes.add(questaoGerada.enunciado());

            QuestaoRequestDTO requestGerado = QuestaoRequestDTO.builder()
                    .enunciado(questaoGerada.enunciado())
                    .comentario(questaoGerada.comentario())
                    .explicacao(questaoGerada.explicacao())
                    .ano(Year.now().getValue())
                    .fonte(FONTE_GERADA_POR_IA)
                    .dificuldade(request.getDificuldade())
                    .tipo(request.getTipo())
                    .geradaPorIA(true)
                    .concursoId(concurso != null ? concurso.getId() : null)
                    .disciplinaId(disciplina.getId())
                    .assuntoId(assunto.getId())
                    .alternativas(converterAlternativasGeradas(questaoGerada.alternativas()))
                    .build();

            geradas.add(cadastrar(requestGerado));
        }

        return geradas;
    }

    private List<AlternativaRequestDTO> converterAlternativasGeradas(List<AlternativaGeradaIA> alternativasGeradas) {
        if (alternativasGeradas == null) {
            return List.of();
        }
        return alternativasGeradas.stream()
                .map(alternativa -> AlternativaRequestDTO.builder()
                        .letra(alternativa.letra())
                        .descricao(alternativa.descricao())
                        .correta(alternativa.correta())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestaoResponseDTO buscarPorId(Long id) {
        return questaoMapper.toResponse(buscarEntidadePorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestaoResponseDTO> listarTodas() {
        return questaoMapper.toResponseList(questaoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestaoAdminResponseDTO> listarTodasParaModeracao() {
        return questaoMapper.toAdminResponseList(questaoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestaoResponseDTO> listarPorDisciplina(Long disciplinaId) {
        Disciplina disciplina = buscarDisciplina(disciplinaId);
        return questaoMapper.toResponseList(questaoRepository.findByDisciplina(disciplina));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestaoResponseDTO> listarPorAssunto(Long assuntoId) {
        Assunto assunto = buscarAssunto(assuntoId);
        return questaoMapper.toResponseList(questaoRepository.findByAssunto(assunto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestaoResponseDTO> listarPorDificuldade(Dificuldade dificuldade) {
        return questaoMapper.toResponseList(questaoRepository.findByDificuldade(dificuldade));
    }

    @Override
    public QuestaoResponseDTO atualizar(Long id, QuestaoRequestDTO request) {
        validarAlternativas(request.getTipo(), request.getAlternativas());

        Questao questao = buscarEntidadePorId(id);

        questaoMapper.updateEntityFromDto(request, questao);
        questao.setDisciplina(buscarDisciplina(request.getDisciplinaId()));
        questao.setAssunto(buscarAssunto(request.getAssuntoId()));
        questao.setConcurso(buscarConcursoOpcional(request.getConcursoId()));
        questao.setUsuario(buscarUsuarioOpcional(request.getUsuarioId()));

        substituirAlternativas(questao, request.getAlternativas());

        return questaoMapper.toResponse(questao);
    }

    @Override
    public void deletar(Long id) {
        Questao questao = buscarEntidadePorId(id);
        questaoRepository.delete(questao);
    }

    /**
     * Para tipos objetivos (múltipla escolha, certo/errado) é obrigatório informar as
     * alternativas com exatamente uma marcada como correta. Discursivas dispensam alternativas.
     */
    private void validarAlternativas(TipoQuestao tipo, List<AlternativaRequestDTO> alternativas) {
        if (tipo == TipoQuestao.DISCURSIVA) {
            return;
        }

        if (alternativas == null || alternativas.isEmpty()) {
            throw new RegraNegocioException("Questões objetivas exigem ao menos uma alternativa.");
        }

        long quantidadeCorretas = alternativas.stream()
                .filter(alternativa -> Boolean.TRUE.equals(alternativa.getCorreta()))
                .count();

        if (quantidadeCorretas != 1) {
            throw new RegraNegocioException("Questões objetivas exigem exatamente uma alternativa correta.");
        }
    }

    /**
     * Substitui o conteúdo da coleção gerenciada pelo Hibernate (em vez de trocar a
     * referência da lista), preservando o orphanRemoval configurado em Questao.alternativas.
     */
    private void substituirAlternativas(Questao questao, List<AlternativaRequestDTO> novasAlternativas) {
        List<Alternativa> alternativasConvertidas = novasAlternativas == null
                ? List.of()
                : alternativaMapper.toEntityList(novasAlternativas);
        alternativasConvertidas.forEach(alternativa -> alternativa.setQuestao(questao));

        if (questao.getAlternativas() == null) {
            questao.setAlternativas(new ArrayList<>());
        }
        questao.getAlternativas().clear();
        questao.getAlternativas().addAll(alternativasConvertidas);
    }

    private Questao buscarEntidadePorId(Long id) {
        return questaoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Questão", id));
    }

    private Disciplina buscarDisciplina(Long disciplinaId) {
        return disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Disciplina", disciplinaId));
    }

    private Assunto buscarAssunto(Long assuntoId) {
        return assuntoRepository.findById(assuntoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Assunto", assuntoId));
    }

    private Concurso buscarConcursoOpcional(Long concursoId) {
        if (concursoId == null) {
            return null;
        }
        return concursoRepository.findById(concursoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Concurso", concursoId));
    }

    private Usuario buscarUsuarioOpcional(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Usuário", usuarioId));
    }

    /**
     * Resolve de qual concurso puxar o contexto de edital indexado (RAG): o edital do
     * concurso atual, se já indexado; senão, o edital mais recente já indexado do mesmo
     * órgão; senão, nenhum (a geração segue sem contexto de edital).
     */
    private Long resolverConcursoParaEdital(Concurso concurso) {
        if (concurso == null) {
            return null;
        }
        if (Boolean.TRUE.equals(concurso.getEditalIndexado())) {
            return concurso.getId();
        }
        return concursoRepository.findFirstByOrgaoAndEditalIndexadoTrueOrderByAnoDesc(concurso.getOrgao())
                .map(Concurso::getId)
                .orElse(null);
    }
}
