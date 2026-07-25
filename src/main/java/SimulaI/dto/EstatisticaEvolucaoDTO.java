package SimulaI.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Um ponto da evolução de desempenho do usuário: percentual de acerto de um simulado
 * finalizado, na data em que foi concluído. A lista ordenada desses pontos dá o gráfico
 * de evolução no tempo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticaEvolucaoDTO {

    private Long simuladoId;

    private LocalDateTime data;

    private Double percentual;
}
