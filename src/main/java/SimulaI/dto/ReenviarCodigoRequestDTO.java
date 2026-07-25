package SimulaI.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import SimulaI.enums.TipoCodigoVerificacao;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReenviarCodigoRequestDTO {

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotNull
    private TipoCodigoVerificacao tipo;
}
