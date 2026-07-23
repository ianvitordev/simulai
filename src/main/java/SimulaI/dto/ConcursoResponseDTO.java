package SimulaI.dto;

import java.util.List;

import SimulaI.enums.StatusConcurso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcursoResponseDTO {
    
    private Long id;

    private String nome;

    private String orgao;

    private String cargo;

    private Integer ano;

    private String banca;

    private StatusConcurso status;

    private String editalUrl;

    private Boolean editalIndexado;

    private List<String> disciplinas;
}
