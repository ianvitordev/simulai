package SimulaI.service;

import java.util.List;

import SimulaI.dto.DisciplinaRequestDTO;
import SimulaI.dto.DisciplinaResponseDTO;

public interface DisciplinaService {

    DisciplinaResponseDTO cadastrar(DisciplinaRequestDTO request);

    DisciplinaResponseDTO buscarPorId(Long id);

    List<DisciplinaResponseDTO> listarTodas();

    DisciplinaResponseDTO atualizar(Long id, DisciplinaRequestDTO request);

    void deletar(Long id);
}
