package SimulaI.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.BancaRequestDTO;
import SimulaI.dto.BancaResponseDTO;
import SimulaI.entity.Banca;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.mapper.BancaMapper;
import SimulaI.repository.BancaRepository;
import SimulaI.service.BancaService;

@Service
@Transactional
public class BancaServiceImpl implements BancaService {

    private final BancaRepository bancaRepository;
    private final BancaMapper bancaMapper;

    public BancaServiceImpl(BancaRepository bancaRepository, BancaMapper bancaMapper) {
        this.bancaRepository = bancaRepository;
        this.bancaMapper = bancaMapper;
    }

    @Override
    public BancaResponseDTO cadastrar(BancaRequestDTO request) {
        if (bancaRepository.existsByNome(request.getNome())) {
            throw new RegistroDuplicadoException("Já existe uma banca cadastrada com o nome: " + request.getNome());
        }

        Banca banca = bancaMapper.toEntity(request);
        Banca bancaSalva = bancaRepository.save(banca);
        return bancaMapper.toResponse(bancaSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public BancaResponseDTO buscarPorId(Long id) {
        Banca banca = buscarEntidadePorId(id);
        return bancaMapper.toResponse(banca);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BancaResponseDTO> listarTodas() {
        return bancaMapper.toResponseList(bancaRepository.findAll());
    }

    @Override
    public BancaResponseDTO atualizar(Long id, BancaRequestDTO request) {
        Banca banca = buscarEntidadePorId(id);

        if (!banca.getNome().equals(request.getNome()) && bancaRepository.existsByNome(request.getNome())) {
            throw new RegistroDuplicadoException("Já existe uma banca cadastrada com o nome: " + request.getNome());
        }

        bancaMapper.updateEntityFromDto(request, banca);
        return bancaMapper.toResponse(banca);
    }

    @Override
    public void deletar(Long id) {
        Banca banca = buscarEntidadePorId(id);
        bancaRepository.delete(banca);
    }

    private Banca buscarEntidadePorId(Long id) {
        return bancaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Banca", id));
    }
}
