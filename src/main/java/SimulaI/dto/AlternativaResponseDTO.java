package SimulaI.dto;

import SimulaI.enums.LetraAlternativa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlternativaResponseDTO {
    
      private Long id;

    private LetraAlternativa letra;

    private String descricao;
}
