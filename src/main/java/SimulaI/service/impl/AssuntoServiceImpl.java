package SimulaI.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.AssuntoRequestDTO;
import SimulaI.dto.AssuntoResponseDTO;
import SimulaI.entity.Assunto;
import SimulaI.entity.Disciplina;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.mapper.AssuntoMapper;
import SimulaI.repository.AssuntoRepository;
import SimulaI.repository.DisciplinaRepository;
import SimulaI.service.AssuntoService;

@Service
@Transactional
public class AssuntoServiceImpl implements AssuntoService {

    private final AssuntoRepository assuntoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoMapper assuntoMapper;

    public AssuntoServiceImpl(AssuntoRepository assuntoRepository,
                               DisciplinaRepository disciplinaRepository,
                               AssuntoMapper assuntoMapper) {
        this.assuntoRepository = assuntoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoMapper = assuntoMapper;
    }

    @Override
    public AssuntoResponseDTO cadastrar(AssuntoRequestDTO request) {
        Disciplina disciplina = buscarDisciplina(request.getDisciplinaId());

        Assunto assunto = assuntoMapper.toEntity(request);
        assunto.setDisciplina(disciplina);

        Assunto assuntoSalvo = assuntoRepository.save(assunto);
        return assuntoMapper.toResponse(assuntoSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public AssuntoResponseDTO buscarPorId(Long id) {
        Assunto assunto = buscarEntidadePorId(id);
        return assuntoMapper.toResponse(assunto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssuntoResponseDTO> listarTodos() {
        return assuntoMapper.toResponseList(assuntoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssuntoResponseDTO> listarPorDisciplina(Long disciplinaId) {
        Disciplina disciplina = buscarDisciplina(disciplinaId);
        return assuntoMapper.toResponseList(assuntoRepository.findByDisciplina(disciplina));
    }

    @Override
    public AssuntoResponseDTO atualizar(Long id, AssuntoRequestDTO request) {
        Assunto assunto = buscarEntidadePorId(id);
        Disciplina disciplina = buscarDisciplina(request.getDisciplinaId());

        assuntoMapper.updateEntityFromDto(request, assunto);
        assunto.setDisciplina(disciplina);

        return assuntoMapper.toResponse(assunto);
    }

    @Override
    public void deletar(Long id) {
        Assunto assunto = buscarEntidadePorId(id);
        assuntoRepository.delete(assunto);
    }

    private Assunto buscarEntidadePorId(Long id) {
        return assuntoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Assunto", id));
    }

    private Disciplina buscarDisciplina(Long disciplinaId) {
        return disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Disciplina", disciplinaId));
    }
}
