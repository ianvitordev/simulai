package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.AssuntoRequestDTO;
import SimulaI.dto.AssuntoResponseDTO;
import SimulaI.entity.Assunto;
import SimulaI.entity.Disciplina;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.mapper.AssuntoMapper;
import SimulaI.repository.AssuntoRepository;
import SimulaI.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
class AssuntoServiceImplTest {

    @Mock
    private AssuntoRepository assuntoRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private AssuntoMapper assuntoMapper;

    @InjectMocks
    private AssuntoServiceImpl assuntoService;

    @Test
    void deveCadastrarAssuntoResolvendoDisciplina() {
        AssuntoRequestDTO request = AssuntoRequestDTO.builder().nome("Atos Administrativos").disciplinaId(10L).build();
        Disciplina disciplina = Disciplina.builder().id(10L).nome("Direito Administrativo").build();
        Assunto entidade = Assunto.builder().nome("Atos Administrativos").build();
        Assunto salvo = Assunto.builder().id(1L).nome("Atos Administrativos").disciplina(disciplina).build();
        AssuntoResponseDTO response = AssuntoResponseDTO.builder().id(1L).nome("Atos Administrativos")
                .disciplina("Direito Administrativo").build();

        when(disciplinaRepository.findById(10L)).thenReturn(Optional.of(disciplina));
        when(assuntoMapper.toEntity(request)).thenReturn(entidade);
        when(assuntoRepository.save(entidade)).thenReturn(salvo);
        when(assuntoMapper.toResponse(salvo)).thenReturn(response);

        AssuntoResponseDTO resultado = assuntoService.cadastrar(request);

        assertThat(resultado.getDisciplina()).isEqualTo("Direito Administrativo");
        assertThat(entidade.getDisciplina()).isEqualTo(disciplina);
        verify(assuntoRepository).save(entidade);
    }

    @Test
    void deveLancarExcecaoAoCadastrarComDisciplinaInexistente() {
        AssuntoRequestDTO request = AssuntoRequestDTO.builder().nome("Atos Administrativos").disciplinaId(99L).build();
        when(disciplinaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assuntoService.cadastrar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoBuscarAssuntoInexistente() {
        when(assuntoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assuntoService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
