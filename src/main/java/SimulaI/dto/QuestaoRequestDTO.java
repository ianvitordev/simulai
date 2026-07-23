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
public class QuestaoRequestDTO {

    private String enunciado;

    private String comentario;

    private String explicacao;

    private Integer ano;

    private String fonte;

    private Dificuldade dificuldade;

    private TipoQuestao tipo;

    private Boolean geradaPorIA;

    private Long concursoId;

    private Long disciplinaId;

    private Long assuntoId;

    private Long usuarioId;

    private List<AlternativaRequestDTO> alternativas;

}