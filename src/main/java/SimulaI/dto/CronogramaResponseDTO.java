package SimulaI.dto;

import java.time.LocalDateTime;
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
public class CronogramaResponseDTO {

    private Long id;

    private LocalDateTime geradoEm;

    private Integer diasPorSemana;

    private Integer horasPorDia;

    private String observacaoGeral;

    private List<ItemCronogramaResponseDTO> itens;
}
