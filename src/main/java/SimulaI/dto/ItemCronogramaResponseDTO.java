package SimulaI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import SimulaI.enums.DiaSemana;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCronogramaResponseDTO {

    private Long id;

    private DiaSemana diaSemana;

    private String disciplina;

    private String assunto;

    private Integer duracaoMinutos;

    private String foco;

    private String justificativa;
}
