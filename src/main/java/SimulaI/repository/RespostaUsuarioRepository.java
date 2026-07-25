package SimulaI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SimulaI.entity.Questao;
import SimulaI.entity.RespostaUsuario;
import SimulaI.entity.Simulado;

public interface RespostaUsuarioRepository extends JpaRepository<RespostaUsuario, Long> {

    Optional<RespostaUsuario> findBySimuladoAndQuestao(Simulado simulado, Questao questao);

    List<RespostaUsuario> findBySimulado(Simulado simulado);

    /**
     * Base para as estatísticas agregadas: todas as respostas de um usuário, através de
     * todos os seus simulados, com questao/disciplina/assunto/simulado já carregados
     * (evita N+1 ao agrupar por disciplina/assunto no Service).
     */
    @Query("SELECT r FROM RespostaUsuario r "
            + "JOIN FETCH r.questao q "
            + "JOIN FETCH q.disciplina "
            + "JOIN FETCH q.assunto "
            + "JOIN FETCH r.simulado s "
            + "WHERE s.usuario.id = :usuarioId")
    List<RespostaUsuario> findParaEstatisticas(@Param("usuarioId") Long usuarioId);
}
