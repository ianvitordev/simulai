package SimulaI.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Disciplina;
import SimulaI.entity.Assunto;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {

    List<Assunto> findByDisciplina(Disciplina disciplina);
}
