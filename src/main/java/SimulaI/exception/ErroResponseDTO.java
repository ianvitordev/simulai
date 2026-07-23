package SimulaI.exception;

import java.time.LocalDateTime;

/**
 * Formato padrão de erro retornado pela API. Não expõe stack trace nem detalhes
 * internos — apenas informação segura para o cliente decidir como agir.
 */
public record ErroResponseDTO(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path) {
}
