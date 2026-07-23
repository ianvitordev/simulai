package SimulaI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Usuario;
import SimulaI.enums.Role;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

}