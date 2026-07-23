package SimulaI.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.DisciplinaRequestDTO;
import SimulaI.dto.DisciplinaResponseDTO;
import SimulaI.entity.Disciplina;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.mapper.DisciplinaMapper;
import SimulaI.repository.DisciplinaRepository;
import SimulaI.service.DisciplinaService;

@Service
@Transactional
public class DisciplinaServiceImpl implements DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaMapper disciplinaMapper;

    public DisciplinaServiceImpl(DisciplinaRepository disciplinaRepository, DisciplinaMapper disciplinaMapper) {
        this.disciplinaRepository = disciplinaRepository;
        this.disciplinaMapper = disciplinaMapper;
    }

    @Override
    public DisciplinaResponseDTO cadastrar(DisciplinaRequestDTO request) {
        if (disciplinaRepository.existsByNome(request.getNome())) {
            throw new RegistroDuplicadoException("Já existe uma disciplina cadastrada com o nome: " + request.getNome());
        }

        Disciplina disciplina = disciplinaMapper.toEntity(request);
        Disciplina disciplinaSalva = disciplinaRepository.save(disciplina);
        return disciplinaMapper.toResponse(disciplinaSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public DisciplinaResponseDTO buscarPorId(Long id) {
        Disciplina disciplina = buscarEntidadePorId(id);
        return disciplinaMapper.toResponse(disciplina);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisciplinaResponseDTO> listarTodas() {
        return disciplinaMapper.toResponseList(disciplinaRepository.findAll());
    }

    @Override
    public DisciplinaResponseDTO atualizar(Long id, DisciplinaRequestDTO request) {
        Disciplina disciplina = buscarEntidadePorId(id);

        if (!disciplina.getNome().equals(request.getNome()) && disciplinaRepository.existsByNome(request.getNome())) {
            throw new RegistroDuplicadoException("Já existe uma disciplina cadastrada com o nome: " + request.getNome());
        }

        disciplinaMapper.updateEntityFromDto(request, disciplina);
        return disciplinaMapper.toResponse(disciplina);
    }

    @Override
    public void deletar(Long id) {
        Disciplina disciplina = buscarEntidadePorId(id);
        disciplinaRepository.delete(disciplina);
    }

    private Disciplina buscarEntidadePorId(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Disciplina", id));
    }
}
