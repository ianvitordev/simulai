package SimulaI.service;

import SimulaI.dto.EstatisticaResponseDTO;

public interface EstatisticaService {

    EstatisticaResponseDTO obterEstatisticas(Long usuarioId);
}
