package SimulaI.service;

import SimulaI.dto.ConfirmarCadastroRequestDTO;
import SimulaI.dto.RedefinirSenhaRequestDTO;
import SimulaI.dto.ReenviarCodigoRequestDTO;
import SimulaI.dto.TokenResponseDTO;

public interface AuthService {

    /** Valida o código de confirmação de cadastro, ativa a conta e já devolve um token (login automático). */
    TokenResponseDTO confirmarCadastro(ConfirmarCadastroRequestDTO request);

    /**
     * Gera e envia um código de redefinição de senha, se o e-mail existir. Nunca lança
     * exceção por e-mail inexistente — evita enumerar contas cadastradas (mesmo
     * princípio já usado no login).
     */
    void esqueciSenha(String email);

    /** Valida o código de redefinição, atualiza a senha e já devolve um token (login automático). */
    TokenResponseDTO redefinirSenha(RedefinirSenhaRequestDTO request);

    /** Reenvia (gera um novo) código pendente do tipo informado, se o e-mail existir. */
    void reenviarCodigo(ReenviarCodigoRequestDTO request);
}
