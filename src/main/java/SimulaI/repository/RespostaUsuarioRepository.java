package SimulaI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.Questao;
import SimulaI.entity.RespostaUsuario;
import SimulaI.entity.Simulado;

public interface RespostaUsuarioRepository extends JpaRepository<RespostaUsuario, Long> {

    Optional<RespostaUsuario> findBySimuladoAndQuestao(Simulado simulado, Questao questao);

    List<RespostaUsuario> findBySimulado(Simulado simulado);
}
