package SimulaI.service.ia;

/**
 * Um item do cronograma decidido pela IA. disciplina/assunto vêm como nome (String), não
 * ID: a IA escolhe dentre a lista de opções informada no prompt (ver
 * {@link GeradorCronogramaIA}), e o Service resolve o nome de volta para a entidade real.
 */
public record ItemCronogramaGeradoIA(
        String diaSemana,
        String disciplina,
        String assunto,
        Integer duracaoMinutos,
        String foco,
        String justificativa) {
}
