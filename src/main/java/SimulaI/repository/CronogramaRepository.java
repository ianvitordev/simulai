package SimulaI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Cronograma;
import SimulaI.entity.Usuario;

public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {

    Optional<Cronograma> findByUsuario(Usuario usuario);
}
