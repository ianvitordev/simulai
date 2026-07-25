package SimulaI.service;

import SimulaI.entity.Usuario;
import SimulaI.enums.TipoCodigoVerificacao;

public interface CodigoVerificacaoService {

    /**
     * Apaga qualquer código pendente do mesmo usuário+tipo, gera um novo código de 6
     * dígitos válido por 15 minutos e envia por e-mail.
     */
    void gerarEEnviar(Usuario usuario, TipoCodigoVerificacao tipo);

    /**
     * Confere o código informado contra o código pendente mais recente do usuário+tipo.
     * Lança RegraNegocioException se não houver código pendente, se estiver expirado, se
     * o limite de tentativas foi excedido, ou se o valor informado não bater — nesse
     * último caso incrementa o contador de tentativas antes de lançar. Em caso de
     * sucesso, marca o código como usado (não pode ser reaproveitado).
     */
    void validarCodigo(Usuario usuario, TipoCodigoVerificacao tipo, String codigoInformado);
}
