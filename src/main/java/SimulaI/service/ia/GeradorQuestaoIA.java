package SimulaI.service.ia;

import java.util.List;

import SimulaI.entity.Assunto;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.TipoQuestao;

public interface GeradorQuestaoIA {

    /**
     * Gera o conteúdo de uma questão via IA, enriquecendo o prompt com contexto do vector
     * store (RAG): trechos do edital indexado sob {@code editalConcursoId}. Recebe
     * entidades (não DTOs) porque é uma chamada interna entre Services, não um limite de
     * Controller — quem decide disciplina/assunto/dificuldade/tipo é o chamador
     * (QuestaoServiceImpl), nunca a IA.
     *
     * @param editalConcursoId id do concurso cujo edital indexado deve ser usado como
     *                         contexto — já resolvido pelo chamador com a lógica de
     *                         fallback (edital do concurso atual, ou o mais recente do
     *                         mesmo órgão); {@code null} se não houver nenhum disponível.
     * @param enunciadosExistentes enunciados de questões já existentes para essa mesma
     *                             disciplina/assunto (do banco + do próprio lote sendo
     *                             gerado), usados para instruir a IA a não repetir nem
     *                             parafrasear — consulta direta ao banco (busca exata por
     *                             assunto), não depende de embeddings/vector store.
     */
    QuestaoGeradaIA gerar(Disciplina disciplina, Assunto assunto, Dificuldade dificuldade,
                          TipoQuestao tipo, Concurso concursoOpcional, Long editalConcursoId,
                          List<String> enunciadosExistentes);
}
