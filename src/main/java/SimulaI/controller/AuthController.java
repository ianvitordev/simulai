package SimulaI.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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

import SimulaI.dto.ConfirmarCadastroRequestDTO;
import SimulaI.dto.EsqueciSenhaRequestDTO;
import SimulaI.dto.LoginRequestDTO;
import SimulaI.dto.RedefinirSenhaRequestDTO;
import SimulaI.dto.ReenviarCodigoRequestDTO;
import SimulaI.dto.TokenResponseDTO;
import SimulaI.service.AuthService;
import SimulaI.service.TokenService;
import SimulaI.service.UsuarioDetailsImpl;

@Tag(name = "Autenticação", description = "Login, confirmação de cadastro e redefinição de senha por código")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService,
                           AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.authService = authService;
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

    @Operation(summary = "Confirmar o cadastro com o código enviado por e-mail (ativa a conta e já loga)")
    @PostMapping("/confirmar-cadastro")
    public ResponseEntity<TokenResponseDTO> confirmarCadastro(@Valid @RequestBody ConfirmarCadastroRequestDTO request) {
        return ResponseEntity.ok(authService.confirmarCadastro(request));
    }

    @Operation(summary = "Solicitar código de redefinição de senha por e-mail")
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequestDTO request) {
        authService.esqueciSenha(request.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(summary = "Redefinir a senha com o código enviado por e-mail (e já logar)")
    @PostMapping("/redefinir-senha")
    public ResponseEntity<TokenResponseDTO> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        return ResponseEntity.ok(authService.redefinirSenha(request));
    }

    @Operation(summary = "Reenviar (gerar um novo) código de confirmação de cadastro ou redefinição de senha")
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<Void> reenviarCodigo(@Valid @RequestBody ReenviarCodigoRequestDTO request) {
        authService.reenviarCodigo(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
