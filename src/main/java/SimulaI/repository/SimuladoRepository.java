package SimulaI.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Simulado;
import SimulaI.entity.Usuario;

public interface SimuladoRepository extends JpaRepository<Simulado, Long> {

    List<Simulado> findByUsuario(Usuario usuario);
}
