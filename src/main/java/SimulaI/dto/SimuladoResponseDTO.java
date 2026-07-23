package SimulaI.dto;

import java.time.LocalDateTime;
import java.util.List;

import SimulaI.enums.StatusSimulado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimuladoResponseDTO {

    private Long id;

    private LocalDateTime dataCriacao;

    private LocalDateTime inicio;

    private LocalDateTime fim;

    private Integer quantidadeQuestoes;

    private Integer tempoLimiteMinutos;

    private StatusSimulado status;

    private String usuario;

    private String concurso;

    private List<QuestaoResponseDTO> questoes;
}
