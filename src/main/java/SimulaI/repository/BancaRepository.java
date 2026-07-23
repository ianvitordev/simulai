package SimulaI.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Banca;

public interface BancaRepository extends JpaRepository<Banca, Long> {

    boolean existsByNome(String nome);
}
