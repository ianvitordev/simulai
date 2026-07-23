package SimulaI.service;

import java.util.List;

import SimulaI.dto.AssuntoRequestDTO;
import SimulaI.dto.AssuntoResponseDTO;

public interface AssuntoService {

    AssuntoResponseDTO cadastrar(AssuntoRequestDTO request);

    AssuntoResponseDTO buscarPorId(Long id);

    List<AssuntoResponseDTO> listarTodos();

    List<AssuntoResponseDTO> listarPorDisciplina(Long disciplinaId);

    AssuntoResponseDTO atualizar(Long id, AssuntoRequestDTO request);

    void deletar(Long id);
}
