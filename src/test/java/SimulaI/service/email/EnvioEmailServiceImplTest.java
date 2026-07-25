package SimulaI.service.email;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.EnvioEmailException;

@ExtendWith(MockitoExtension.class)
class EnvioEmailServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono<ResponseEntity<Void>> monoResponse;

    private EnvioEmailServiceImpl envioEmailService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        envioEmailService = new EnvioEmailServiceImpl(webClientBuilder,
                "service-id-teste", "template-id-teste", "public-key-teste", "private-key-teste");
    }

    @SuppressWarnings("unchecked")
    private void mockarCadeiaWebClient() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(org.mockito.ArgumentMatchers.anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(monoResponse);
    }

    @Test
    void deveEnviarCodigoDeVerificacaoSemLancarExcecao() {
        mockarCadeiaWebClient();
        when(monoResponse.block()).thenReturn(ResponseEntity.ok().build());

        assertThatCode(() -> envioEmailService.enviarCodigoVerificacao(
                "usuario@exemplo.com", "Fulano", "123456", TipoCodigoVerificacao.CONFIRMACAO_CADASTRO))
                .doesNotThrowAnyException();
    }

    @Test
    void deveEncapsularFalhaDeEnvioEmEnvioEmailException() {
        mockarCadeiaWebClient();
        when(monoResponse.block()).thenThrow(new RuntimeException("serviço indisponível"));

        assertThatThrownBy(() -> envioEmailService.enviarCodigoVerificacao(
                "usuario@exemplo.com", "Fulano", "123456", TipoCodigoVerificacao.REDEFINICAO_SENHA))
                .isInstanceOf(EnvioEmailException.class);
    }
}
