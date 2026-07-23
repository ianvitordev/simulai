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

import SimulaI.dto.DisciplinaRequestDTO;
import SimulaI.dto.DisciplinaResponseDTO;
import SimulaI.entity.Disciplina;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.mapper.DisciplinaMapper;
import SimulaI.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
class DisciplinaServiceImplTest {

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private DisciplinaMapper disciplinaMapper;

    @InjectMocks
    private DisciplinaServiceImpl disciplinaService;

    @Test
    void deveCadastrarDisciplinaQuandoNomeNaoExiste() {
        DisciplinaRequestDTO request = DisciplinaRequestDTO.builder().nome("Direito Administrativo").descricao("d").build();
        Disciplina entidade = Disciplina.builder().nome("Direito Administrativo").descricao("d").build();
        Disciplina salva = Disciplina.builder().id(1L).nome("Direito Administrativo").descricao("d").build();
        DisciplinaResponseDTO response = DisciplinaResponseDTO.builder().id(1L).nome("Direito Administrativo").descricao("d").build();

        when(disciplinaRepository.existsByNome("Direito Administrativo")).thenReturn(false);
        when(disciplinaMapper.toEntity(request)).thenReturn(entidade);
        when(disciplinaRepository.save(entidade)).thenReturn(salva);
        when(disciplinaMapper.toResponse(salva)).thenReturn(response);

        DisciplinaResponseDTO resultado = disciplinaService.cadastrar(request);

        assertThat(resultado.getId()).isEqualTo(1L);
        verify(disciplinaRepository).save(entidade);
    }

    @Test
    void deveLancarExcecaoAoCadastrarDisciplinaComNomeDuplicado() {
        DisciplinaRequestDTO request = DisciplinaRequestDTO.builder().nome("Direito Administrativo").descricao("d").build();
        when(disciplinaRepository.existsByNome("Direito Administrativo")).thenReturn(true);

        assertThatThrownBy(() -> disciplinaService.cadastrar(request))
                .isInstanceOf(RegistroDuplicadoException.class);

        verify(disciplinaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarDisciplinaInexistente() {
        when(disciplinaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disciplinaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveDeletarDisciplinaExistente() {
        Disciplina disciplina = Disciplina.builder().id(1L).nome("Direito Administrativo").build();
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));

        disciplinaService.deletar(1L);

        verify(disciplinaRepository).delete(disciplina);
    }
}
