package SimulaI.config;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import SimulaI.exception.ErroResponseDTO;

/**
 * Limita tentativas de POST nas rotas públicas de autenticação por IP (token bucket em
 * memória, via Bucket4j) — dificulta brute force de senha/código e spam de e-mail de
 * verificação. Em memória porque o deploy roda numa única instância (Render free tier)
 * — sem necessidade de estado distribuído. Um bucket por IP cobre todas as rotas juntas
 * (mesma superfície de abuso "autenticação"), não uma por rota.
 */
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> ROTAS_LIMITADAS = Set.of(
            "/api/auth/login",
            "/api/auth/confirmar-cadastro",
            "/api/auth/esqueci-senha",
            "/api/auth/redefinir-senha",
            "/api/auth/reenviar-codigo");

    private static final int CAPACIDADE = 5;
    private static final Duration JANELA = Duration.ofMinutes(1);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Bucket> bucketsPorIp = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!HttpMethod.POST.matches(request.getMethod()) || !ROTAS_LIMITADAS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = bucketsPorIp.computeIfAbsent(resolverIpCliente(request), ip -> criarBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long segundosParaEsperar = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(segundosParaEsperar));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(), HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Muitas tentativas. Tente novamente em instantes.", request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), erro);
    }

    private Bucket criarBucket() {
        Bandwidth limite = Bandwidth.builder().capacity(CAPACIDADE).refillGreedy(CAPACIDADE, JANELA).build();
        return Bucket.builder().addLimit(limite).build();
    }

    /**
     * O Render/Cloudflare fazem proxy na frente da aplicação — request.getRemoteAddr()
     * sozinho devolveria sempre o IP do proxy, não o do cliente real.
     */
    private String resolverIpCliente(HttpServletRequest request) {
        String encaminhadoPor = request.getHeader("X-Forwarded-For");
        if (encaminhadoPor != null && !encaminhadoPor.isBlank()) {
            return encaminhadoPor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
