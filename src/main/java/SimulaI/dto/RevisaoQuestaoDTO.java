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
public class RevisaoQuestaoDTO {

    private Long questaoId;

    private String enunciado;

    private String explicacao;

    private Boolean respondida;

    private Boolean acertou;

    private List<RevisaoAlternativaDTO> alternativas;
}
