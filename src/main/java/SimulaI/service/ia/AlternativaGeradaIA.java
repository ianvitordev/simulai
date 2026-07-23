package SimulaI.service.ia;

import SimulaI.enums.LetraAlternativa;

/**
 * Contrato de saída estruturada da IA para uma alternativa — convertido pelo
 * StructuredOutputConverter do Spring AI a partir da resposta em JSON do modelo.
 */
public record AlternativaGeradaIA(
        LetraAlternativa letra,
        String descricao,
        boolean correta) {
}
