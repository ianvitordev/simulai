package SimulaI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Concurso;

public interface ConcursoRepository extends JpaRepository<Concurso, Long> {

    /**
     * Usado como fallback de RAG (etapa 10): quando o concurso pedido não tem edital
     * indexado, busca o edital mais recente já indexado do mesmo órgão.
     */
    Optional<Concurso> findFirstByOrgaoAndEditalIndexadoTrueOrderByAnoDesc(String orgao);
}
