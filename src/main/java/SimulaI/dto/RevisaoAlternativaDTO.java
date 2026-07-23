package SimulaI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import SimulaI.enums.LetraAlternativa;

/**
 * Ao contrário de AlternativaResponseDTO (usado durante a prova, nunca revela o
 * gabarito), este DTO só é montado para simulados já FINALIZADOs — por isso pode
 * expor "correta" e "marcadaPeloUsuario" com segurança.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisaoAlternativaDTO {

    private Long id;

    private LetraAlternativa letra;

    private String descricao;

    private Boolean correta;

    private Boolean marcadaPeloUsuario;
}
