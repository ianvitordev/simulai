package SimulaI.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import SimulaI.entity.Usuario;
import SimulaI.service.TokenService;

@Service
public class TokenServiceImpl implements TokenService {

    private final SecretKey chave;
    private final long expiracaoMinutos;

    public TokenServiceImpl(@Value("${simulai.jwt.secret}") String segredo,
                             @Value("${simulai.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMinutos = expiracaoMinutos;
    }

    @Override
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(expiracaoMinutos, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("usuarioId", usuario.getId())
                .claim("role", usuario.getRole().name())
                .setIssuedAt(Date.from(agora))
                .setExpiration(Date.from(expiracao))
                .signWith(chave, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validarToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String extrairEmail(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public Long extrairUsuarioId(String token) {
        Object valor = parseClaims(token).get("usuarioId");
        return valor == null ? null : Long.valueOf(valor.toString());
    }

    @Override
    public long getExpiracaoSegundos() {
        return expiracaoMinutos * 60;
    }

    /**
     * Claims numéricas do JJWT 0.11.x podem desserializar como Integer em vez de Long
     * dependendo do valor — por isso extrairUsuarioId lê o valor bruto e converte via
     * String, em vez de usar Claims.get(String, Long.class) (evita RequiredTypeException).
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(chave)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
