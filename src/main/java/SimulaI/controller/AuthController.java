package SimulaI.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import SimulaI.dto.LoginRequestDTO;
import SimulaI.dto.TokenResponseDTO;
import SimulaI.service.TokenService;
import SimulaI.service.UsuarioDetailsImpl;

@Tag(name = "Autenticação", description = "Login e emissão de tokens JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * Credenciais inválidas lançam AuthenticationException (o Spring já normaliza
     * "usuário não existe" e "senha errada" na mesma exceção), tratada de forma genérica
     * pelo GlobalExceptionHandler para não permitir enumeração de emails cadastrados.
     */
    @Operation(summary = "Autenticar com email/senha e obter um token JWT")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));

        UsuarioDetailsImpl principal = (UsuarioDetailsImpl) authentication.getPrincipal();
        String token = tokenService.gerarToken(principal.getUsuario());

        TokenResponseDTO response = TokenResponseDTO.builder()
                .accessToken(token)
                .tipo("Bearer")
                .expiraEmSegundos(tokenService.getExpiracaoSegundos())
                .build();

        return ResponseEntity.ok(response);
    }
}
