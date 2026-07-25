package SimulaI.service.ia;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import SimulaI.enums.DiaSemana;
import SimulaI.exception.GeracaoIAException;

@Service
public class GeradorCronogramaIAImpl implements GeradorCronogramaIA {

    private final ChatClient chatClient;

    public GeradorCronogramaIAImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        Você é um orientador de estudos especialista em preparação para concursos públicos
                        brasileiros. Monta cronogramas de estudo semanais realistas, priorizando o tempo e a
                        frequência das disciplinas/assuntos onde o desempenho do aluno é mais fraco, sem
                        abandonar completamente os pontos fortes (uma revisão espaçada é sempre bem-vinda).
                        """)
                .build();
    }

    @Override
    public CronogramaGeradoIA gerar(List<OpcaoEstudoIA> opcoes, Integer diasPorSemana, Integer horasPorDia) {
        String prompt = montarPrompt(opcoes, diasPorSemana, horasPorDia);

        CronogramaGeradoIA cronogramaGerado;
        try {
            cronogramaGerado = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(CronogramaGeradoIA.class);
        } catch (Exception e) {
            throw new GeracaoIAException("Falha ao chamar o provedor de IA: " + e.getMessage(), e);
        }

        if (cronogramaGerado == null) {
            throw new GeracaoIAException("A IA não retornou nenhum conteúdo.");
        }

        validarFormato(cronogramaGerado, opcoes);
        return cronogramaGerado;
    }

    private String montarPrompt(List<OpcaoEstudoIA> opcoes, Integer diasPorSemana, Integer horasPorDia) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(("Monte um cronograma de estudos semanal para um aluno que tem %d dia(s) por semana "
                + "disponíveis, com aproximadamente %d hora(s) de estudo por dia.%n")
                .formatted(diasPorSemana, horasPorDia));

        boolean semDados = opcoes.stream().allMatch(opcao -> opcao.totalRespondidas() == 0);
        if (semDados) {
            prompt.append("O aluno ainda não respondeu nenhuma questão — distribua as disciplinas/assuntos "
                    + "abaixo de forma equilibrada entre os dias disponíveis, sem priorizar nenhuma em especial.\n");
        } else {
            prompt.append("Priorize disciplinas/assuntos com menor percentual de acerto (mais tempo e/ou mais "
                    + "vezes por semana), sem abandonar totalmente os de percentual mais alto — inclua ao menos "
                    + "uma revisão rápida deles.\n");
        }

        prompt.append("\nEscolha SOMENTE dentre estas disciplinas/assuntos (use o nome exatamente como está "
                + "escrito aqui), não invente nenhum fora desta lista:\n");
        opcoes.forEach(opcao -> prompt.append("- Disciplina: %s | Assunto: %s | Desempenho atual: %s%n"
                .formatted(opcao.disciplina(), opcao.assunto(), descreverDesempenho(opcao))));

        prompt.append(("\nPara o campo diaSemana de cada item, use exatamente um destes valores (em maiúsculas, "
                + "sem acento): SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO. Distribua os itens ao "
                + "longo de %d dia(s) distintos da semana, respeitando o tempo diário informado. Preencha "
                + "duracaoMinutos com a duração daquele item em minutos, foco com um resumo curto do que "
                + "estudar/praticar naquele item, e justificativa explicando por que aquele item foi priorizado "
                + "(referencie o percentual de acerto quando fizer sentido). Preencha também observacaoGeral com "
                + "um resumo curto da estratégia do cronograma como um todo.")
                .formatted(diasPorSemana));

        return prompt.toString();
    }

    private String descreverDesempenho(OpcaoEstudoIA opcao) {
        if (opcao.totalRespondidas() == 0) {
            return "sem dados ainda";
        }
        return "%.0f%% de acerto em %d questão(ões) respondida(s)"
                .formatted(opcao.percentualAcerto(), opcao.totalRespondidas());
    }

    /**
     * Confere se a IA respeitou o contrato pedido — mesmo espírito de
     * GeradorQuestaoIAImpl.validarFormato: pega cedo um formato inconsistente (dia da
     * semana inválido, disciplina/assunto fora da lista informada) em vez de deixar a
     * falha aparecer de forma confusa mais adiante, na persistência.
     */
    private void validarFormato(CronogramaGeradoIA cronogramaGerado, List<OpcaoEstudoIA> opcoes) {
        List<ItemCronogramaGeradoIA> itens = cronogramaGerado.itens();
        if (itens == null || itens.isEmpty()) {
            throw new GeracaoIAException("A IA não retornou nenhum item de cronograma.");
        }

        Set<String> chavesValidas = opcoes.stream()
                .map(opcao -> chave(opcao.disciplina(), opcao.assunto()))
                .collect(Collectors.toSet());

        for (ItemCronogramaGeradoIA item : itens) {
            try {
                DiaSemana.fromTextoLivre(item.diaSemana());
            } catch (IllegalArgumentException e) {
                throw new GeracaoIAException(
                        "A IA retornou um dia da semana inválido: '%s'.".formatted(item.diaSemana()));
            }

            if (item.duracaoMinutos() == null || item.duracaoMinutos() <= 0) {
                throw new GeracaoIAException("A IA retornou um item de cronograma com duração inválida.");
            }

            if (!chavesValidas.contains(chave(item.disciplina(), item.assunto()))) {
                throw new GeracaoIAException(
                        "A IA retornou uma disciplina/assunto fora da lista informada: '%s' / '%s'."
                                .formatted(item.disciplina(), item.assunto()));
            }
        }
    }

    private String chave(String disciplina, String assunto) {
        String disciplinaNormalizada = disciplina == null ? "" : disciplina.trim().toLowerCase(Locale.ROOT);
        String assuntoNormalizado = assunto == null ? "" : assunto.trim().toLowerCase(Locale.ROOT);
        return disciplinaNormalizada + "|" + assuntoNormalizado;
    }
}
