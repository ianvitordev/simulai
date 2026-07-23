package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import SimulaI.entity.Usuario;
import SimulaI.enums.Role;

class TokenServiceImplTest {

    private static final String SEGREDO_TESTE = "chave-secreta-fixa-apenas-para-os-testes-0000000000000";

    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl(SEGREDO_TESTE, 60);
    }

    private Usuario usuarioTeste() {
        return Usuario.builder().id(42L).nome("Maria").email("maria@teste.com").role(Role.ALUNO).build();
    }

    @Test
    void deveGerarTokenValidoComClaimsCorretas() {
        Usuario usuario = usuarioTeste();

        String token = tokenService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(tokenService.validarToken(token)).isTrue();
        assertThat(tokenService.extrairEmail(token)).isEqualTo("maria@teste.com");
        assertThat(tokenService.extrairUsuarioId(token)).isEqualTo(42L);
    }

    @Test
    void deveInvalidarTokenMalformado() {
        assertThat(tokenService.validarToken("isto-nao-e-um-jwt")).isFalse();
    }

    @Test
    void deveInvalidarTokenAssinadoComOutraChave() {
        TokenServiceImpl outroServico = new TokenServiceImpl("outra-chave-secreta-completamente-diferente-000000000", 60);
        String token = outroServico.gerarToken(usuarioTeste());

        assertThat(tokenService.validarToken(token)).isFalse();
    }

    @Test
    void deveInvalidarTokenExpirado() throws InterruptedException {
        TokenServiceImpl servicoExpiracaoCurta = new TokenServiceImpl(SEGREDO_TESTE, 0);
        String token = servicoExpiracaoCurta.gerarToken(usuarioTeste());

        Thread.sleep(50);

        assertThat(servicoExpiracaoCurta.validarToken(token)).isFalse();
    }

    @Test
    void deveExporExpiracaoEmSegundosConsistenteComOsMinutosConfigurados() {
        assertThat(tokenService.getExpiracaoSegundos()).isEqualTo(60L * 60);
    }
}
