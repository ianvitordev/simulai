package SimulaI.service.ia;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import SimulaI.entity.Assunto;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.enums.Dificuldade;
import SimulaI.enums.TipoQuestao;
import SimulaI.exception.GeracaoIAException;

@Service
public class GeradorQuestaoIAImpl implements GeradorQuestaoIA {

    private static final Logger log = LoggerFactory.getLogger(GeradorQuestaoIAImpl.class);

    private static final int TOP_K_EDITAL = 3;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public GeradorQuestaoIAImpl(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        Você é um especialista em elaborar questões de concursos públicos brasileiros,
                        no estilo das principais bancas organizadoras (CESPE/CEBRASPE, FGV, FCC, VUNESP,
                        entre outras). Gere questões tecnicamente corretas, sem ambiguidade, com linguagem
                        formal e objetiva, e explicações claras e completas para a resposta correta.
                        """)
                .build();
    }

    @Override
    public QuestaoGeradaIA gerar(Disciplina disciplina, Assunto assunto, Dificuldade dificuldade,
                                  TipoQuestao tipo, Concurso concursoOpcional, Long editalConcursoId,
                                  List<String> enunciadosExistentes) {
        String contextoEdital = buscarContextoEdital(editalConcursoId);

        String prompt = montarPrompt(disciplina, assunto, dificuldade, tipo, concursoOpcional,
                contextoEdital, enunciadosExistentes);

        QuestaoGeradaIA questaoGerada;
        try {
            questaoGerada = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(QuestaoGeradaIA.class);
        } catch (Exception e) {
            throw new GeracaoIAException("Falha ao chamar o provedor de IA: " + e.getMessage(), e);
        }

        if (questaoGerada == null) {
            throw new GeracaoIAException("A IA não retornou nenhum conteúdo.");
        }

        validarFormato(questaoGerada, tipo);
        return questaoGerada;
    }

    /**
     * RAG é um reforço de qualidade, não um requisito rígido: se o vector store falhar
     * (ex.: instabilidade momentânea), a geração deve seguir sem o contexto extra em vez
     * de derrubar a funcionalidade principal (criar a questão).
     */
    private String buscarContextoEdital(Long editalConcursoId) {
        if (editalConcursoId == null) {
            return null;
        }

        try {
            SearchRequest request = SearchRequest.builder()
                    .query("conteúdo programático e regras do edital")
                    .topK(TOP_K_EDITAL)
                    .filterExpression("tipo == 'edital' && concursoId == %d".formatted(editalConcursoId))
                    .build();

            return juntarTextos(vectorStore.similaritySearch(request));
        } catch (Exception e) {
            log.warn("Falha ao buscar contexto de edital no vector store (concursoId={}): {}",
                    editalConcursoId, e.getMessage());
            return null;
        }
    }

    private String juntarTextos(List<Document> documentos) {
        if (documentos == null || documentos.isEmpty()) {
            return null;
        }
        return documentos.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
    }

    private String montarPrompt(Disciplina disciplina, Assunto assunto, Dificuldade dificuldade,
                                 TipoQuestao tipo, Concurso concurso, String contextoEdital,
                                 List<String> enunciadosExistentes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Gere uma questão do tipo %s.%n".formatted(descreverTipo(tipo)));
        prompt.append("Disciplina: %s.%n".formatted(disciplina.getNome()));
        prompt.append("Assunto: %s.%n".formatted(assunto.getNome()));
        prompt.append("Nível de dificuldade: %s.%n".formatted(dificuldade.name()));

        if (concurso != null) {
            prompt.append(("Contexto: a questão deve seguir o estilo da banca '%s' e ter complexidade "
                    + "compatível com o cargo '%s'.%n")
                    .formatted(concurso.getBanca().getNome(), concurso.getCargo()));
        }

        if (contextoEdital != null) {
            prompt.append("\nTrechos do edital deste concurso — use apenas como referência de escopo e "
                    + "regras cobradas, NÃO copie literalmente:\n")
                    .append(contextoEdital).append('\n');
        }

        if (enunciadosExistentes != null && !enunciadosExistentes.isEmpty()) {
            prompt.append("\nQuestões já existentes sobre este mesmo assunto — a nova questão precisa ser "
                    + "claramente diferente de todas elas (outro ângulo, outro sub-tópico, outra pegadinha), "
                    + "NÃO repita nem parafraseie nenhuma:\n");
            enunciadosExistentes.forEach(enunciado -> prompt.append("- ").append(enunciado).append('\n'));
        }

        prompt.append(instrucaoAlternativas(tipo));
        return prompt.toString();
    }

    private String descreverTipo(TipoQuestao tipo) {
        return switch (tipo) {
            case MULTIPLA_ESCOLHA -> "múltipla escolha";
            case CERTO_ERRADO -> "certo ou errado";
            case DISCURSIVA -> "dissertativa";
        };
    }

    private String instrucaoAlternativas(TipoQuestao tipo) {
        String instrucaoBase = switch (tipo) {
            case MULTIPLA_ESCOLHA ->
                    "Forneça exatamente 5 alternativas (letras A a E), com exatamente uma marcada como correta. "
                            + "O campo explicacao é obrigatório: explique de forma clara e completa por que a "
                            + "alternativa correta está certa e, brevemente, por que cada uma das demais está errada.";
            case CERTO_ERRADO ->
                    "Forneça exatamente 2 alternativas (letra A = Certo, letra B = Errado), com exatamente uma "
                            + "marcada como correta. O campo explicacao é obrigatório: explique de forma clara e "
                            + "completa por que a resposta correta está certa.";
            case DISCURSIVA ->
                    "Não gere alternativas (retorne lista vazia). O campo explicacao é obrigatório: descreva os "
                            + "pontos que uma resposta completa precisa abordar.";
        };

        String instrucaoTextoPlano = "\nO enunciado é exibido como texto simples, sem nenhuma formatação visual "
                + "(sem negrito, itálico, sublinhado ou grifo). NUNCA escreva algo como 'o termo sublinhado', "
                + "'a palavra em negrito' ou 'o trecho grifado' — se precisar se referir a uma palavra ou trecho "
                + "específico da frase, cite-o entre aspas diretamente no enunciado (ex.: 'qual a função sintática "
                + "do termo \"rapidamente\" na frase abaixo?').";

        String instrucaoAlternativasDistintas = tipo == TipoQuestao.MULTIPLA_ESCOLHA
                ? "\nCada uma das 5 alternativas precisa ter um texto totalmente distinto das demais — nunca "
                        + "repita a mesma alternativa (ou uma alternativa quase idêntica) com letras diferentes."
                : "";

        return instrucaoBase + instrucaoTextoPlano + instrucaoAlternativasDistintas;
    }

    /**
     * Confere se a IA respeitou o contrato pedido antes de devolver ao chamador — pega
     * cedo um formato inconsistente (ex.: 4 alternativas em vez de 5), em vez de deixar
     * a falha aparecer de forma confusa mais adiante, na regra de negócio do QuestaoService.
     */
    private void validarFormato(QuestaoGeradaIA questaoGerada, TipoQuestao tipo) {
        if (questaoGerada.enunciado() == null || questaoGerada.enunciado().isBlank()) {
            throw new GeracaoIAException("A IA retornou uma questão sem enunciado.");
        }

        if (questaoGerada.explicacao() == null || questaoGerada.explicacao().isBlank()) {
            throw new GeracaoIAException("A IA retornou uma questão sem explicação da resposta.");
        }

        int quantidadeEsperada = switch (tipo) {
            case MULTIPLA_ESCOLHA -> 5;
            case CERTO_ERRADO -> 2;
            case DISCURSIVA -> 0;
        };

        List<AlternativaGeradaIA> alternativas =
                questaoGerada.alternativas() == null ? List.of() : questaoGerada.alternativas();

        if (alternativas.size() != quantidadeEsperada) {
            throw new GeracaoIAException(
                    "A IA retornou %d alternativa(s), mas eram esperadas %d para o tipo %s."
                            .formatted(alternativas.size(), quantidadeEsperada, tipo));
        }

        if (quantidadeEsperada > 0) {
            long corretas = alternativas.stream().filter(AlternativaGeradaIA::correta).count();
            if (corretas != 1) {
                throw new GeracaoIAException(
                        "A IA retornou %d alternativa(s) marcada(s) como correta; era esperada exatamente 1."
                                .formatted(corretas));
            }

            long textosDistintos = alternativas.stream()
                    .map(alternativa -> alternativa.descricao().strip().toLowerCase())
                    .distinct()
                    .count();
            if (textosDistintos != alternativas.size()) {
                throw new GeracaoIAException(
                        "A IA retornou alternativas com texto duplicado (esperava %d textos distintos, veio %d)."
                                .formatted(alternativas.size(), textosDistintos));
            }
        }
    }
}
