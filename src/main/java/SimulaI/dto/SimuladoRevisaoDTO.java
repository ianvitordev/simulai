package SimulaI.dto;

import java.util.List;

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
public class SimuladoRevisaoDTO {

    private Long simuladoId;

    private List<RevisaoQuestaoDTO> questoes;
}
