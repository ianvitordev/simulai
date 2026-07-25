package SimulaI.service.email;

import SimulaI.enums.TipoCodigoVerificacao;

public interface EnvioEmailService {

    /**
     * Envia o e-mail com o código de verificação (confirmação de cadastro ou
     * redefinição de senha, conforme {@code tipo}). Falha de envio vira
     * {@code EnvioEmailException} — quem chama não trata degradação graciosa aqui, o
     * código é a única forma de o usuário prosseguir no fluxo.
     */
    void enviarCodigoVerificacao(String destinatario, String nomeDestinatario, String codigo,
                                  TipoCodigoVerificacao tipo);
}
