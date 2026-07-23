package SimulaI.dto;

import java.util.List;

import SimulaI.enums.Dificuldade;
import SimulaI.enums.TipoQuestao;
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
public class QuestaoResponseDTO {

    private Long id;

    private String enunciado;

    private String comentario;

    private String explicacao;

    private Integer ano;

    private String fonte;

    private Dificuldade dificuldade;

    private TipoQuestao tipo;

    private Boolean geradaPorIA;

    private String concurso;

    private String disciplina;

    private String assunto;

    private List<AlternativaResponseDTO> alternativas;

}
