package SimulaI.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import SimulaI.entity.Assunto;
import SimulaI.entity.Disciplina;
import SimulaI.entity.Questao;
import SimulaI.enums.Dificuldade;

public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao> {

    List<Questao> findByDisciplina(Disciplina disciplina);

    List<Questao> findByAssunto(Assunto assunto);

    /**
     * Usada para alimentar o prompt de geração via IA com as questões mais recentes do
     * assunto (evitar repetição) — ordenada da mais nova pra mais antiga para permitir
     * limitar a quantidade sem precisar carregar o histórico inteiro.
     */
    List<Questao> findByAssuntoOrderByIdDesc(Assunto assunto);

    List<Questao> findByDificuldade(Dificuldade dificuldade);

    List<Questao> findByDisciplinaIn(List<Disciplina> disciplinas);

}