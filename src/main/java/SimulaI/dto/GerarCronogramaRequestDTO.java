package SimulaI.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class GerarCronogramaRequestDTO {

    @NotNull
    @Min(1)
    @Max(7)
    private Integer diasPorSemana;

    @NotNull
    @Min(1)
    @Max(8)
    private Integer horasPorDia;
}
