package SimulaI.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import SimulaI.enums.Dificuldade;
import SimulaI.enums.TipoQuestao;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GerarQuestaoIARequestDTO {

    @NotNull
    private Long disciplinaId;

    @NotNull
    private Long assuntoId;

    /**
     * Opcional: se informado, a IA é instruída a seguir o estilo da banca e a
     * complexidade do cargo daquele concurso.
     */
    private Long concursoId;

    @NotNull
    private Dificuldade dificuldade;

    @NotNull
    private TipoQuestao tipo;

    @NotNull
    @Positive
    @Max(10)
    private Integer quantidade;
}
