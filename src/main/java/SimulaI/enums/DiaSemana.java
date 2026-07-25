package SimulaI.enums;

import java.text.Normalizer;

public enum DiaSemana {
    SEGUNDA,
    TERCA,
    QUARTA,
    QUINTA,
    SEXTA,
    SABADO,
    DOMINGO;

    /**
     * A IA nem sempre respeita o formato pedido no prompt (maiúsculas, sem acento) —
     * normaliza antes de resolver o enum em vez de rejeitar variações como "Terça" ou
     * "sábado". Lança IllegalArgumentException (mesma exceção de Enum.valueOf) se, mesmo
     * assim, não bater com nenhum valor.
     */
    public static DiaSemana fromTextoLivre(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Dia da semana não informado.");
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return DiaSemana.valueOf(semAcento.trim().toUpperCase());
    }
}
