package SimulaI.exception;

/**
 * Lançada quando a chamada à IA falha (erro de rede/autenticação com o provedor) ou
 * quando a IA retorna um conteúdo que não respeita o contrato pedido (ex.: número de
 * alternativas incompatível com o tipo de questão solicitado).
 */
public class GeracaoIAException extends RuntimeException {

    public GeracaoIAException(String mensagem) {
        super(mensagem);
    }

    public GeracaoIAException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
