package SimulaI.service;

import java.util.List;

import SimulaI.dto.ConcursoRequestDTO;
import SimulaI.dto.ConcursoResponseDTO;

public interface ConcursoService {

    ConcursoResponseDTO cadastrar(ConcursoRequestDTO request);

    ConcursoResponseDTO buscarPorId(Long id);

    List<ConcursoResponseDTO> listarTodos();

    /** Concursos com pelo menos uma "Prova" real (questões não geradas por IA) disponível. */
    List<ConcursoResponseDTO> listarProvasDisponiveis();

    ConcursoResponseDTO atualizar(Long id, ConcursoRequestDTO request);

    void deletar(Long id);

    ConcursoResponseDTO adicionarDisciplina(Long concursoId, Long disciplinaId);

    ConcursoResponseDTO removerDisciplina(Long concursoId, Long disciplinaId);

    /**
     * Baixa e indexa o edital (RAG) a partir da editalUrl do concurso. Retorna a
     * quantidade de trechos (chunks) indexados no vector store.
     */
    int indexarEdital(Long concursoId);
}
