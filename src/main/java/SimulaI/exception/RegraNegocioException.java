package SimulaI.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: questão sem alternativa correta,
 * simulado sem questões suficientes no banco para a quantidade solicitada).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
