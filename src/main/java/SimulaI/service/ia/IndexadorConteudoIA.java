package SimulaI.service.ia;

import SimulaI.entity.Concurso;
import SimulaI.entity.Questao;

public interface IndexadorConteudoIA {

    /**
     * Indexa o enunciado/explicação de uma questão recém-criada no vector store, para
     * que futuras gerações da IA possam usá-la como referência de estilo/conteúdo
     * ("questões antigas prováveis de cair").
     */
    void indexarQuestao(Questao questao);

    /**
     * Baixa o PDF de {@code concurso.getEditalUrl()}, extrai o texto, fatia em chunks e
     * indexa no vector store. Retorna a quantidade de chunks indexados.
     */
    int indexarEdital(Concurso concurso);
}
