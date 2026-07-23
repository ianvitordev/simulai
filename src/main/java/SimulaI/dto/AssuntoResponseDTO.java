package SimulaI.dto;

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
public class AssuntoResponseDTO {

    private Long id;

    private String nome;

    private String descricao;

    private String disciplina;
}
