package SimulaI.service;

import java.util.List;

import SimulaI.dto.GerarSimuladoRequestDTO;
import SimulaI.dto.RespostaUsuarioRequestDTO;
import SimulaI.dto.RespostaUsuarioResponseDTO;
import SimulaI.dto.SimuladoResponseDTO;
import SimulaI.dto.SimuladoResultadoDTO;
import SimulaI.dto.SimuladoRevisaoDTO;

public interface SimuladoService {

    SimuladoResponseDTO gerar(Long usuarioId, GerarSimuladoRequestDTO request);

    SimuladoResponseDTO buscarPorId(Long id);

    List<SimuladoResponseDTO> listarPorUsuario(Long usuarioId);

    SimuladoResponseDTO iniciar(Long simuladoId);

    RespostaUsuarioResponseDTO responderQuestao(Long simuladoId, RespostaUsuarioRequestDTO request);

    SimuladoResultadoDTO finalizar(Long simuladoId);

    SimuladoResponseDTO cancelar(Long simuladoId);

    /**
     * Só disponível após FINALIZADO: revela o gabarito completo de cada questão e qual
     * alternativa o usuário marcou, para estudo pós-simulado.
     */
    SimuladoRevisaoDTO revisar(Long simuladoId);
}
