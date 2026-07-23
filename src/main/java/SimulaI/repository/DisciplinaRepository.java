package SimulaI.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Disciplina;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    boolean existsByNome(String nome);
}
