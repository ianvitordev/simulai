package SimulaI.service.ia;

import java.util.List;

/**
 * Contrato de saída estruturada da IA para um cronograma de estudos completo.
 */
public record CronogramaGeradoIA(String observacaoGeral, List<ItemCronogramaGeradoIA> itens) {
}
