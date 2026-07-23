package SimulaI.service;

import java.util.List;

import SimulaI.dto.BancaRequestDTO;
import SimulaI.dto.BancaResponseDTO;

public interface BancaService {

    BancaResponseDTO cadastrar(BancaRequestDTO request);

    BancaResponseDTO buscarPorId(Long id);

    List<BancaResponseDTO> listarTodas();

    BancaResponseDTO atualizar(Long id, BancaRequestDTO request);

    void deletar(Long id);
}
