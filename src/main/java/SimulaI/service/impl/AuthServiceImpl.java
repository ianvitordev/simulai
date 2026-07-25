package SimulaI.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import SimulaI.dto.ConfirmarCadastroRequestDTO;
import SimulaI.dto.RedefinirSenhaRequestDTO;
import SimulaI.dto.ReenviarCodigoRequestDTO;
import SimulaI.dto.TokenResponseDTO;
import SimulaI.entity.Usuario;
import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.AuthService;
import SimulaI.service.CodigoVerificacaoService;
import SimulaI.service.TokenService;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CodigoVerificacaoService codigoVerificacaoService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                            CodigoVerificacaoService codigoVerificacaoService,
                            PasswordEncoder passwordEncoder,
                            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.codigoVerificacaoService = codigoVerificacaoService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public TokenResponseDTO confirmarCadastro(ConfirmarCadastroRequestDTO request) {
        Usuario usuario = buscarUsuario(request.getEmail());
        codigoVerificacaoService.validarCodigo(usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO,
                request.getCodigo());

        usuario.setEmailVerificado(true);
        return gerarTokenResponse(usuario);
    }

    @Override
    public void esqueciSenha(String email) {
        usuarioRepository.findByEmail(email).ifPresentOrElse(
                usuario -> codigoVerificacaoService.gerarEEnviar(usuario, TipoCodigoVerificacao.REDEFINICAO_SENHA),
                () -> log.info("Esqueci-senha solicitado para e-mail não cadastrado: {}", email));
    }

    @Override
    public TokenResponseDTO redefinirSenha(RedefinirSenhaRequestDTO request) {
        Usuario usuario = buscarUsuario(request.getEmail());
        codigoVerificacaoService.validarCodigo(usuario, TipoCodigoVerificacao.REDEFINICAO_SENHA, request.getCodigo());

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        return gerarTokenResponse(usuario);
    }

    @Override
    public void reenviarCodigo(ReenviarCodigoRequestDTO request) {
        usuarioRepository.findByEmail(request.getEmail()).ifPresentOrElse(
                usuario -> codigoVerificacaoService.gerarEEnviar(usuario, request.getTipo()),
                () -> log.info("Reenvio de código solicitado para e-mail não cadastrado: {}", request.getEmail()));
    }

    private TokenResponseDTO gerarTokenResponse(Usuario usuario) {
        String token = tokenService.gerarToken(usuario);
        return TokenResponseDTO.builder()
                .accessToken(token)
                .tipo("Bearer")
                .expiraEmSegundos(tokenService.getExpiracaoSegundos())
                .build();
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com email: " + email));
    }
}
