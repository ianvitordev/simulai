package SimulaI.service.ia;

/**
 * Uma opção válida de disciplina/assunto que a IA pode escolher ao montar o cronograma,
 * junto com o desempenho atual do usuário nela (ou {@code totalRespondidas == 0} se ele
 * ainda não respondeu nenhuma questão daquele assunto). Montada pelo
 * CronogramaServiceImpl a partir do catálogo (Assunto) + EstatisticaService.
 */
public record OpcaoEstudoIA(String disciplina, String assunto, Double percentualAcerto, Integer totalRespondidas) {
}
