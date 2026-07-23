package SimulaI.dto;

import SimulaI.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;

    private String nome;

    private String email;

    private Role role;

}