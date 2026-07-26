package SimulaI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import SimulaI.entity.Concurso;

public interface ConcursoRepository extends JpaRepository<Concurso, Long> {

    /**
     * Usado como fallback de RAG (etapa 10): quando o concurso pedido não tem edital
     * indexado, busca o edital mais recente já indexado do mesmo órgão.
     */
    Optional<Concurso> findFirstByOrgaoAndEditalIndexadoTrueOrderByAnoDesc(String orgao);

    /** "Provas" disponíveis: concursos com pelo menos uma questão real (não gerada por IA). */
    @Query("SELECT DISTINCT q.concurso FROM Questao q WHERE q.geradaPorIA = false AND q.concurso IS NOT NULL")
    List<Concurso> findComQuestoesReais();
}
