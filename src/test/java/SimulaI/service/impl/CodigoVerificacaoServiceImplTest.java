package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.entity.CodigoVerificacao;
import SimulaI.entity.Usuario;
import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.RegraNegocioException;
import SimulaI.repository.CodigoVerificacaoRepository;
import SimulaI.service.email.EnvioEmailService;

@ExtendWith(MockitoExtension.class)
class CodigoVerificacaoServiceImplTest {

    @Mock
    private CodigoVerificacaoRepository codigoVerificacaoRepository;

    @Mock
    private EnvioEmailService envioEmailService;

    @InjectMocks
    private CodigoVerificacaoServiceImpl codigoVerificacaoService;

    private Usuario usuarioTeste() {
        return Usuario.builder().id(1L).nome("Maria").email("maria@teste.com").build();
    }

    @Test
    void deveApagarCodigosPendentesGerarNovoEEnviarPorEmail() {
        Usuario usuario = usuarioTeste();
        CodigoVerificacao anterior = CodigoVerificacao.builder().id(10L).build();
        when(codigoVerificacaoRepository.findByUsuarioAndTipoAndUsadoFalse(usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO))
                .thenReturn(List.of(anterior));

        codigoVerificacaoService.gerarEEnviar(usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO);

        verify(codigoVerificacaoRepository).deleteAll(List.of(anterior));

        ArgumentCaptor<CodigoVerificacao> captor = ArgumentCaptor.forClass(CodigoVerificacao.class);
        verify(codigoVerificacaoRepository).save(captor.capture());
        CodigoVerificacao salvo = captor.getValue();
        assertThat(salvo.getCodigo()).hasSize(6);
        assertThat(salvo.getUsuario()).isEqualTo(usuario);
        assertThat(salvo.getTipo()).isEqualTo(TipoCodigoVerificacao.CONFIRMACAO_CADASTRO);
        assertThat(salvo.getExpiraEm()).isAfter(LocalDateTime.now());

        verify(envioEmailService).enviarCodigoVerificacao(eq("maria@teste.com"), eq("Maria"), anyString(),
                eq(TipoCodigoVerificacao.CONFIRMACAO_CADASTRO));
    }

    @Test
    void deveValidarCodigoCorretoEMarcarComoUsado() {
        Usuario usuario = usuarioTeste();
        CodigoVerificacao codigo = CodigoVerificacao.builder()
                .codigo("123456").expiraEm(LocalDateTime.now().plusMinutes(10)).tentativas(0).usado(false).build();
        when(codigoVerificacaoRepository.findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(
                usuario, TipoCodigoVerificacao.REDEFINICAO_SENHA)).thenReturn(Optional.of(codigo));

        codigoVerificacaoService.validarCodigo(usuario, TipoCodigoVerificacao.REDEFINICAO_SENHA, "123456");

        assertThat(codigo.getUsado()).isTrue();
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaCodigoPendente() {
        Usuario usuario = usuarioTeste();
        when(codigoVerificacaoRepository.findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> codigoVerificacaoService.validarCodigo(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO, "123456"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoQuandoCodigoExpirado() {
        Usuario usuario = usuarioTeste();
        CodigoVerificacao codigo = CodigoVerificacao.builder()
                .codigo("123456").expiraEm(LocalDateTime.now().minusMinutes(1)).tentativas(0).usado(false).build();
        when(codigoVerificacaoRepository.findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO)).thenReturn(Optional.of(codigo));

        assertThatThrownBy(() -> codigoVerificacaoService.validarCodigo(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO, "123456"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveLancarExcecaoQuandoLimiteDeTentativasExcedido() {
        Usuario usuario = usuarioTeste();
        CodigoVerificacao codigo = CodigoVerificacao.builder()
                .codigo("123456").expiraEm(LocalDateTime.now().plusMinutes(10)).tentativas(5).usado(false).build();
        when(codigoVerificacaoRepository.findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO)).thenReturn(Optional.of(codigo));

        assertThatThrownBy(() -> codigoVerificacaoService.validarCodigo(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO, "123456"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveIncrementarTentativasQuandoCodigoInformadoEstaErrado() {
        Usuario usuario = usuarioTeste();
        CodigoVerificacao codigo = CodigoVerificacao.builder()
                .codigo("123456").expiraEm(LocalDateTime.now().plusMinutes(10)).tentativas(0).usado(false).build();
        when(codigoVerificacaoRepository.findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO)).thenReturn(Optional.of(codigo));

        assertThatThrownBy(() -> codigoVerificacaoService.validarCodigo(
                usuario, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO, "000000"))
                .isInstanceOf(RegraNegocioException.class);

        assertThat(codigo.getTentativas()).isEqualTo(1);
        assertThat(codigo.getUsado()).isFalse();
    }
}
