package SimulaI.service.ia;

import java.util.List;

/**
 * Contrato de saída estruturada da IA para uma questão completa. Contém apenas o que a
 * IA deve decidir (texto e alternativas) — disciplina, assunto, dificuldade e tipo são
 * determinados por quem pede a geração, não pela IA, e por isso não aparecem aqui.
 */
public record QuestaoGeradaIA(
        String enunciado,
        String comentario,
        String explicacao,
        List<AlternativaGeradaIA> alternativas) {
}
