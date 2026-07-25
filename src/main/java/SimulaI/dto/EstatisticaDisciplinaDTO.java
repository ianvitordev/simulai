package SimulaI.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Agregação de desempenho por disciplina, com o detalhamento por assunto dentro. Não é
 * mapeamento direto de entidade — montado manualmente no Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticaDisciplinaDTO {

    private String disciplina;

    private Integer totalRespondidas;

    private Integer acertos;

    private Double percentual;

    private List<EstatisticaAssuntoDTO> porAssunto;
}
