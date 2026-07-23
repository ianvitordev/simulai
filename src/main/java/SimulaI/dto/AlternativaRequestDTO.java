package SimulaI.dto;

import SimulaI.enums.LetraAlternativa;
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
public class AlternativaRequestDTO {
    
    private LetraAlternativa letra;

    private String descricao;

    private Boolean correta;

}
