package SimulaI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Agregação de desempenho por assunto, dentro de uma disciplina. Não é mapeamento direto
 * de entidade — montado manualmente no Service a partir das respostas do usuário.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticaAssuntoDTO {

    private String assunto;

    private Integer totalRespondidas;

    private Integer acertos;

    private Double percentual;
}
