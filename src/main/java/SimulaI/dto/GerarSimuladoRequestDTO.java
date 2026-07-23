package SimulaI.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import SimulaI.enums.Dificuldade;
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
public class GerarSimuladoRequestDTO {

    /**
     * Opcional: se informado, o pool de questões é restrito às disciplinas do edital
     * desse concurso. Se nulo, o sorteio considera todo o banco de questões ativas.
     */
    private Long concursoId;

    @NotNull
    @Positive
    private Integer quantidadeQuestoes;

    /**
     * Opcional: se nulo, o sorteio não filtra por dificuldade.
     */
    private Dificuldade dificuldade;

    @PositiveOrZero
    private Integer tempoLimiteMinutos;

}
