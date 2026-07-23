package SimulaI.exception;

/**
 * Lançada quando um recurso buscado por identificador (id, email, etc.) não existe.
 * Reutilizada por todos os Services para evitar uma exceção por entidade.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException porId(String entidade, Long id) {
        return new RecursoNaoEncontradoException(entidade + " não encontrado(a) com id: " + id);
    }
}
