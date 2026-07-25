package SimulaI.service.email;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.EnvioEmailException;

@Service
public class EnvioEmailServiceImpl implements EnvioEmailService {

    private static final String EMAILJS_URL = "https://api.emailjs.com/api/v1.0/email/send";

    private final WebClient webClient;
    private final String serviceId;
    private final String templateId;
    private final String publicKey;
    private final String privateKey;

    public EnvioEmailServiceImpl(WebClient.Builder webClientBuilder,
                                  @Value("${simulai.emailjs.service-id}") String serviceId,
                                  @Value("${simulai.emailjs.template-id}") String templateId,
                                  @Value("${simulai.emailjs.public-key}") String publicKey,
                                  @Value("${simulai.emailjs.private-key}") String privateKey) {
        this.webClient = webClientBuilder.build();
        this.serviceId = serviceId;
        this.templateId = templateId;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void enviarCodigoVerificacao(String destinatario, String nomeDestinatario, String codigo,
                                         TipoCodigoVerificacao tipo) {
        Map<String, Object> corpo = Map.of(
                "service_id", serviceId,
                "template_id", templateId,
                "user_id", publicKey,
                "accessToken", privateKey,
                "template_params", Map.of(
                        "to_email", destinatario,
                        "nome", nomeDestinatario,
                        "assunto", assunto(tipo),
                        "instrucao", instrucao(tipo),
                        "codigo", codigo));

        try {
            webClient.post()
                    .uri(EMAILJS_URL)
                    .bodyValue(corpo)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new EnvioEmailException("Falha ao enviar e-mail: " + e.getMessage(), e);
        }
    }

    private String assunto(TipoCodigoVerificacao tipo) {
        return switch (tipo) {
            case CONFIRMACAO_CADASTRO -> "Confirme seu cadastro no SimulaI";
            case REDEFINICAO_SENHA -> "Redefinição de senha - SimulaI";
        };
    }

    private String instrucao(TipoCodigoVerificacao tipo) {
        return switch (tipo) {
            case CONFIRMACAO_CADASTRO -> "Use o código abaixo para confirmar seu cadastro no SimulaI:";
            case REDEFINICAO_SENHA -> "Use o código abaixo para redefinir sua senha no SimulaI. "
                    + "Se você não solicitou isso, ignore este e-mail.";
        };
    }
}
