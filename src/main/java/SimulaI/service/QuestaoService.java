package SimulaI.service;

import java.util.List;

import SimulaI.dto.GerarQuestaoIARequestDTO;
import SimulaI.dto.QuestaoAdminResponseDTO;
import SimulaI.dto.QuestaoRequestDTO;
import SimulaI.dto.QuestaoResponseDTO;
import SimulaI.enums.Dificuldade;

public interface QuestaoService {

    QuestaoResponseDTO cadastrar(QuestaoRequestDTO request);

    /**
     * Único mecanismo de criação de questões exposto publicamente: não há endpoint de
     * cadastro manual, apenas geração via IA (que internamente reaproveita cadastrar()).
     */
    List<QuestaoResponseDTO> gerarViaIA(GerarQuestaoIARequestDTO request);

    QuestaoResponseDTO buscarPorId(Long id);

    List<QuestaoResponseDTO> listarTodas();

    /**
     * Listagem para moderação (ADMIN): expõe o gabarito de cada alternativa. Deve
     * permanecer restrita a ADMIN no SecurityConfig.
     */
    List<QuestaoAdminResponseDTO> listarTodasParaModeracao();

    List<QuestaoResponseDTO> listarPorDisciplina(Long disciplinaId);

    List<QuestaoResponseDTO> listarPorAssunto(Long assuntoId);

    List<QuestaoResponseDTO> listarPorDificuldade(Dificuldade dificuldade);

    QuestaoResponseDTO atualizar(Long id, QuestaoRequestDTO request);

    void deletar(Long id);
}
