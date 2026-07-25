package SimulaI.service;

import SimulaI.dto.CronogramaResponseDTO;
import SimulaI.dto.GerarCronogramaRequestDTO;

public interface CronogramaService {

    CronogramaResponseDTO gerar(Long usuarioId, GerarCronogramaRequestDTO request);

    CronogramaResponseDTO obterAtual(Long usuarioId);
}
