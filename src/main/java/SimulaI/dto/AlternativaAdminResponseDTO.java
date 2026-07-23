package SimulaI.dto;

import SimulaI.enums.LetraAlternativa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Variante de {@link AlternativaResponseDTO} exclusiva para moderação (ADMIN): inclui o
 * campo {@code correta}, que nunca deve ser exposto na leitura padrão usada por alunos.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlternativaAdminResponseDTO {

    private Long id;

    private LetraAlternativa letra;

    private String descricao;

    private Boolean correta;
}
