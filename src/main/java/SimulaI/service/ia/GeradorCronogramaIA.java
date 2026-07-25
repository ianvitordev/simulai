package SimulaI.service.ia;

import java.util.List;

public interface GeradorCronogramaIA {

    /**
     * Gera um cronograma de estudos via IA, priorizando as opções com pior desempenho.
     * Recebe a lista de opções (não entidades) porque quem decide o que é uma opção
     * válida é o chamador (CronogramaServiceImpl) — a IA só escolhe dentre elas, nunca
     * inventa disciplina/assunto fora da lista.
     */
    CronogramaGeradoIA gerar(List<OpcaoEstudoIA> opcoes, Integer diasPorSemana, Integer horasPorDia);
}
