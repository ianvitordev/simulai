package SimulaI.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estatísticas completas de desempenho de um usuário, agregando todas as respostas dele
 * através de todos os simulados (exceto os CANCELADO). Não é mapeamento direto de
 * entidade — montado manualmente no Service a partir de {@code RespostaUsuario}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticaResponseDTO {

    private Integer totalRespondidas;

    private Integer totalAcertos;

    private Double percentualGeral;

    private Integer totalSimuladosFinalizados;

    private Long tempoTotalSegundos;

    private List<EstatisticaDisciplinaDTO> porDisciplina;

    private List<EstatisticaEvolucaoDTO> evolucao;
}
