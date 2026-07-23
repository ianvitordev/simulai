package SimulaI.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Tradutor único de exceções para respostas HTTP. Centralizar aqui evita que cada
 * Controller precise de try/catch próprio e garante um formato de erro consistente
 * em toda a API.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                                        HttpServletRequest request) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    public ResponseEntity<ErroResponseDTO> handleRegistroDuplicado(RegistroDuplicadoException ex,
                                                                     HttpServletRequest request) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleRegraNegocio(RegraNegocioException ex,
                                                                HttpServletRequest request) {
        return construirResposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    /**
     * Defesa contra condição de corrida: o Service verifica unicidade antes de salvar,
     * mas duas requisições concorrentes ainda podem colidir na constraint do banco.
     * Sem isso, essa falha vazaria como 500 com detalhes da exceção SQL.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleIntegridadeDados(DataIntegrityViolationException ex,
                                                                    HttpServletRequest request) {
        log.warn("Violação de integridade de dados em {}: {}", request.getRequestURI(), ex.getMessage());
        return construirResposta(HttpStatus.CONFLICT,
                "Os dados enviados violam uma restrição de integridade (registro duplicado ou vínculo inválido).",
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoDTO> handleValidacao(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<ErroValidacaoDTO.CampoErro> camposInvalidos = ex.getBindingResult().getFieldErrors().stream()
                .map(erroCampo -> new ErroValidacaoDTO.CampoErro(erroCampo.getField(), erroCampo.getDefaultMessage()))
                .toList();

        ErroResponseDTO erroBase = new ErroResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Um ou mais campos são inválidos.",
                request.getRequestURI());

        return ResponseEntity.badRequest().body(new ErroValidacaoDTO(erroBase, camposInvalidos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleCorpoInvalido(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        return construirResposta(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou malformado.", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        String mensagem = "O parâmetro '%s' recebeu um valor inválido: '%s'.".formatted(ex.getName(), ex.getValue());
        return construirResposta(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    /**
     * Cobre falha de login (email inexistente ou senha errada). Mensagem genérica de
     * propósito: distinguir os dois casos permitiria enumerar emails cadastrados.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleAutenticacao(AuthenticationException ex, HttpServletRequest request) {
        return construirResposta(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos.", request);
    }

    /**
     * Disparada pelo @PreAuthorize dos Controllers quando o usuário autenticado não é
     * dono do recurso (ex.: tentar responder o simulado de outro usuário) nem ADMIN.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAcessoNegado(AccessDeniedException ex, HttpServletRequest request) {
        return construirResposta(HttpStatus.FORBIDDEN, "Você não tem permissão para executar esta ação.", request);
    }

    /**
     * 502: a falha é de uma dependência externa (o provedor de IA), não da nossa API —
     * seja por erro de chamada, seja por a IA ter devolvido um formato que não respeita
     * o contrato pedido (ex.: número de alternativas incompatível com o tipo de questão).
     */
    @ExceptionHandler(GeracaoIAException.class)
    public ResponseEntity<ErroResponseDTO> handleGeracaoIA(GeracaoIAException ex, HttpServletRequest request) {
        log.warn("Falha na geração de questão via IA em {}: {}", request.getRequestURI(), ex.getMessage());
        return construirResposta(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /**
     * Catch-all: qualquer exceção não mapeada é logada com stack trace completo para
     * diagnóstico interno, mas o cliente recebe só uma mensagem genérica — nunca o
     * detalhe da exceção, para não vazar informação interna da aplicação.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGenerico(Exception ex, HttpServletRequest request) {
        log.error("Erro não tratado em {}", request.getRequestURI(), ex);
        return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", request);
    }

    private ResponseEntity<ErroResponseDTO> construirResposta(HttpStatus status, String mensagem,
                                                                HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
