package SimulaI.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import SimulaI.entity.CodigoVerificacao;
import SimulaI.entity.Usuario;
import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.RegraNegocioException;
import SimulaI.repository.CodigoVerificacaoRepository;
import SimulaI.service.CodigoVerificacaoService;
import SimulaI.service.email.EnvioEmailService;

@Slf4j
@Service
@Transactional
public class CodigoVerificacaoServiceImpl implements CodigoVerificacaoService {

    private static final int VALIDADE_MINUTOS = 15;
    private static final int MAX_TENTATIVAS = 5;

    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final EnvioEmailService envioEmailService;
    private final SecureRandom random = new SecureRandom();

    public CodigoVerificacaoServiceImpl(CodigoVerificacaoRepository codigoVerificacaoRepository,
                                         EnvioEmailService envioEmailService) {
        this.codigoVerificacaoRepository = codigoVerificacaoRepository;
        this.envioEmailService = envioEmailService;
    }

    @Override
    public void gerarEEnviar(Usuario usuario, TipoCodigoVerificacao tipo) {
        List<CodigoVerificacao> pendentes = codigoVerificacaoRepository
                .findByUsuarioAndTipoAndUsadoFalse(usuario, tipo);
        codigoVerificacaoRepository.deleteAll(pendentes);

        String codigo = gerarCodigoAleatorio();
        CodigoVerificacao novoCodigo = CodigoVerificacao.builder()
                .usuario(usuario)
                .tipo(tipo)
                .codigo(codigo)
                .expiraEm(LocalDateTime.now().plusMinutes(VALIDADE_MINUTOS))
                .build();
        codigoVerificacaoRepository.save(novoCodigo);

        // Logado sempre (não só em falha de envio) pra dar pra testar o fluxo local sem
        // depender de configurar o Brevo antes.
        log.info("Código de verificação gerado para {} (tipo={}): {}", usuario.getEmail(), tipo, codigo);

        envioEmailService.enviarCodigoVerificacao(usuario.getEmail(), usuario.getNome(), codigo, tipo);
    }

    @Override
    public void validarCodigo(Usuario usuario, TipoCodigoVerificacao tipo, String codigoInformado) {
        CodigoVerificacao codigo = codigoVerificacaoRepository
                .findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(usuario, tipo)
                .orElseThrow(() -> new RegraNegocioException(
                        "Nenhum código pendente para essa solicitação — peça um novo código."));

        if (codigo.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Esse código expirou — peça um novo código.");
        }

        if (codigo.getTentativas() >= MAX_TENTATIVAS) {
            throw new RegraNegocioException("Número máximo de tentativas excedido — peça um novo código.");
        }

        if (!codigo.getCodigo().equals(codigoInformado)) {
            codigo.setTentativas(codigo.getTentativas() + 1);
            throw new RegraNegocioException("Código inválido.");
        }

        codigo.setUsado(true);
    }

    private String gerarCodigoAleatorio() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
