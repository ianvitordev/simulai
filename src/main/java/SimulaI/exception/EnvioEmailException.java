package SimulaI.exception;

/**
 * Lançada quando o envio de e-mail (código de confirmação/redefinição) falha. Mesmo
 * molde de GeracaoIAException: falha de uma dependência externa (o provedor de e-mail),
 * não da nossa API — mapeada para 502 no GlobalExceptionHandler.
 */
public class EnvioEmailException extends RuntimeException {

    public EnvioEmailException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
