package SimulaI.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class RespostaUsuarioRequestDTO {

    @NotNull
    private Long questaoId;

    @NotNull
    private Long alternativaMarcadaId;

    @PositiveOrZero
    private Integer tempoRespostaSegundos;
}
