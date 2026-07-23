package SimulaI.exception;

import java.util.List;

/**
 * Estende o formato padrão de erro com a lista de campos inválidos, usado nas
 * respostas de falha do Bean Validation (@Valid nos DTOs de requisição).
 */
public record ErroValidacaoDTO(
        ErroResponseDTO erro,
        List<CampoErro> camposInvalidos) {

    public record CampoErro(String campo, String mensagem) {
    }
}
