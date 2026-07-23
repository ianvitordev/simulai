package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.BancaRequestDTO;
import SimulaI.dto.BancaResponseDTO;
import SimulaI.entity.Banca;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.mapper.BancaMapper;
import SimulaI.repository.BancaRepository;

@ExtendWith(MockitoExtension.class)
class BancaServiceImplTest {

    @Mock
    private BancaRepository bancaRepository;

    @Mock
    private BancaMapper bancaMapper;

    @InjectMocks
    private BancaServiceImpl bancaService;

    @Test
    void deveCadastrarBancaQuandoNomeNaoExiste() {
        BancaRequestDTO request = BancaRequestDTO.builder().nome("CESPE").descricao("Banca federal").build();
        Banca entidade = Banca.builder().nome("CESPE").descricao("Banca federal").build();
        Banca salva = Banca.builder().id(1L).nome("CESPE").descricao("Banca federal").build();
        BancaResponseDTO response = BancaResponseDTO.builder().id(1L).nome("CESPE").descricao("Banca federal").build();

        when(bancaRepository.existsByNome("CESPE")).thenReturn(false);
        when(bancaMapper.toEntity(request)).thenReturn(entidade);
        when(bancaRepository.save(entidade)).thenReturn(salva);
        when(bancaMapper.toResponse(salva)).thenReturn(response);

        BancaResponseDTO resultado = bancaService.cadastrar(request);

        assertThat(resultado.getId()).isEqualTo(1L);
        verify(bancaRepository).save(entidade);
    }

    @Test
    void deveLancarExcecaoAoCadastrarBancaComNomeDuplicado() {
        BancaRequestDTO request = BancaRequestDTO.builder().nome("CESPE").descricao("x").build();
        when(bancaRepository.existsByNome("CESPE")).thenReturn(true);

        assertThatThrownBy(() -> bancaService.cadastrar(request))
                .isInstanceOf(RegistroDuplicadoException.class);

        verify(bancaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarBancaInexistente() {
        when(bancaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bancaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveDeletarBancaExistente() {
        Banca banca = Banca.builder().id(1L).nome("CESPE").build();
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));

        bancaService.deletar(1L);

        verify(bancaRepository).delete(banca);
    }
}
