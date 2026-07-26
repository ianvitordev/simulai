package SimulaI.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.ConcursoRequestDTO;
import SimulaI.dto.ConcursoResponseDTO;
import SimulaI.entity.Banca;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.ConcursoMapper;
import SimulaI.repository.BancaRepository;
import SimulaI.repository.ConcursoRepository;
import SimulaI.repository.DisciplinaRepository;
import SimulaI.service.ConcursoService;
import SimulaI.service.ia.IndexadorConteudoIA;

@Service
@Transactional
public class ConcursoServiceImpl implements ConcursoService {

    private final ConcursoRepository concursoRepository;
    private final BancaRepository bancaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ConcursoMapper concursoMapper;
    private final IndexadorConteudoIA indexadorConteudoIA;

    public ConcursoServiceImpl(ConcursoRepository concursoRepository,
                                BancaRepository bancaRepository,
                                DisciplinaRepository disciplinaRepository,
                                ConcursoMapper concursoMapper,
                                IndexadorConteudoIA indexadorConteudoIA) {
        this.concursoRepository = concursoRepository;
        this.bancaRepository = bancaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.concursoMapper = concursoMapper;
        this.indexadorConteudoIA = indexadorConteudoIA;
    }

    @Override
    public ConcursoResponseDTO cadastrar(ConcursoRequestDTO request) {
        Banca banca = buscarBanca(request.getBancaId());

        Concurso concurso = concursoMapper.toEntity(request);
        concurso.setBanca(banca);
        concurso.setDisciplinas(new ArrayList<>());

        Concurso concursoSalvo = concursoRepository.save(concurso);
        return concursoMapper.toResponse(concursoSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public ConcursoResponseDTO buscarPorId(Long id) {
        Concurso concurso = buscarEntidadePorId(id);
        return concursoMapper.toResponse(concurso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConcursoResponseDTO> listarTodos() {
        return concursoMapper.toResponseList(concursoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConcursoResponseDTO> listarProvasDisponiveis() {
        return concursoMapper.toResponseList(concursoRepository.findComQuestoesReais());
    }

    @Override
    public ConcursoResponseDTO atualizar(Long id, ConcursoRequestDTO request) {
        Concurso concurso = buscarEntidadePorId(id);
        Banca banca = buscarBanca(request.getBancaId());

        concursoMapper.updateEntityFromDto(request, concurso);
        concurso.setBanca(banca);

        return concursoMapper.toResponse(concurso);
    }

    @Override
    public void deletar(Long id) {
        Concurso concurso = buscarEntidadePorId(id);
        concursoRepository.delete(concurso);
    }

    @Override
    public ConcursoResponseDTO adicionarDisciplina(Long concursoId, Long disciplinaId) {
        Concurso concurso = buscarEntidadePorId(concursoId);
        Disciplina disciplina = buscarDisciplina(disciplinaId);

        if (concurso.getDisciplinas() == null) {
            concurso.setDisciplinas(new ArrayList<>());
        }
        if (concurso.getDisciplinas().contains(disciplina)) {
            throw new RegraNegocioException("A disciplina já está associada a este concurso.");
        }
        concurso.getDisciplinas().add(disciplina);

        return concursoMapper.toResponse(concurso);
    }

    @Override
    public ConcursoResponseDTO removerDisciplina(Long concursoId, Long disciplinaId) {
        Concurso concurso = buscarEntidadePorId(concursoId);
        Disciplina disciplina = buscarDisciplina(disciplinaId);

        if (concurso.getDisciplinas() == null || !concurso.getDisciplinas().remove(disciplina)) {
            throw new RegraNegocioException("A disciplina não está associada a este concurso.");
        }

        return concursoMapper.toResponse(concurso);
    }

    @Override
    public int indexarEdital(Long concursoId) {
        Concurso concurso = buscarEntidadePorId(concursoId);

        int chunksIndexados = indexadorConteudoIA.indexarEdital(concurso);
        concurso.setEditalIndexado(true);

        return chunksIndexados;
    }

    private Concurso buscarEntidadePorId(Long id) {
        return concursoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Concurso", id));
    }

    private Banca buscarBanca(Long bancaId) {
        return bancaRepository.findById(bancaId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Banca", bancaId));
    }

    private Disciplina buscarDisciplina(Long disciplinaId) {
        return disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Disciplina", disciplinaId));
    }
}
