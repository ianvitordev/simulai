package SimulaI.exception;

/**
 * Lançada ao violar uma restrição de unicidade de negócio (email, nome de banca/disciplina, etc.)
 * antes de chegar ao banco, evitando depender da exceção genérica de constraint do JPA.
 */
public class RegistroDuplicadoException extends RuntimeException {

    public RegistroDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
