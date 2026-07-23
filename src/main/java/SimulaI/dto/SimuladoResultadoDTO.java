package SimulaI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resultado calculado ao finalizar um simulado. Não é um mapeamento direto de entidade
 * (é uma agregação sobre as respostas), por isso é montado manualmente no Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimuladoResultadoDTO {

    private Long simuladoId;

    private Integer totalQuestoes;

    private Integer acertos;

    private Integer erros;

    private Double percentualAcerto;

    private Integer tempoTotalSegundos;
}
