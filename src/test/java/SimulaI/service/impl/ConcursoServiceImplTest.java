package SimulaI.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import SimulaI.dto.ConcursoRequestDTO;
import SimulaI.dto.ConcursoResponseDTO;
import SimulaI.entity.Banca;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.ConcursoMapper;
import SimulaI.repository.BancaRepository;
import SimulaI.repository.ConcursoRepository;
import SimulaI.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
class ConcursoServiceImplTest {

    @Mock
    private ConcursoRepository concursoRepository;

    @Mock
    private BancaRepository bancaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private ConcursoMapper concursoMapper;

    @InjectMocks
    private ConcursoServiceImpl concursoService;

    @Test
    void deveCadastrarConcursoResolvendoBanca() {
        ConcursoRequestDTO request = ConcursoRequestDTO.builder().nome("Concurso X").bancaId(5L).build();
        Banca banca = Banca.builder().id(5L).nome("CESPE").build();
        Concurso entidade = Concurso.builder().nome("Concurso X").build();
        Concurso salvo = Concurso.builder().id(1L).nome("Concurso X").banca(banca).build();
        ConcursoResponseDTO response = ConcursoResponseDTO.builder().id(1L).nome("Concurso X").banca("CESPE").build();

        when(bancaRepository.findById(5L)).thenReturn(Optional.of(banca));
        when(concursoMapper.toEntity(request)).thenReturn(entidade);
        when(concursoRepository.save(entidade)).thenReturn(salvo);
        when(concursoMapper.toResponse(salvo)).thenReturn(response);

        ConcursoResponseDTO resultado = concursoService.cadastrar(request);

        assertThat(resultado.getBanca()).isEqualTo("CESPE");
        assertThat(entidade.getBanca()).isEqualTo(banca);
        assertThat(entidade.getDisciplinas()).isEmpty();
    }

    @Test
    void deveLancarExcecaoQuandoBancaNaoExisteAoCadastrar() {
        ConcursoRequestDTO request = ConcursoRequestDTO.builder().nome("Concurso X").bancaId(99L).build();
        when(bancaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> concursoService.cadastrar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveAdicionarDisciplinaAoConcurso() {
        Disciplina disciplina = Disciplina.builder().id(2L).nome("Direito Administrativo").build();
        Concurso concurso = Concurso.builder().id(1L).nome("Concurso X").disciplinas(new ArrayList<>()).build();

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(disciplinaRepository.findById(2L)).thenReturn(Optional.of(disciplina));
        when(concursoMapper.toResponse(concurso)).thenReturn(
                ConcursoResponseDTO.builder().id(1L).nome("Concurso X").build());

        concursoService.adicionarDisciplina(1L, 2L);

        assertThat(concurso.getDisciplinas()).containsExactly(disciplina);
    }

    @Test
    void deveLancarExcecaoAoAdicionarDisciplinaJaAssociada() {
        Disciplina disciplina = Disciplina.builder().id(2L).nome("Direito Administrativo").build();
        Concurso concurso = Concurso.builder().id(1L).nome("Concurso X")
                .disciplinas(new ArrayList<>(List.of(disciplina))).build();

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(disciplinaRepository.findById(2L)).thenReturn(Optional.of(disciplina));

        assertThatThrownBy(() -> concursoService.adicionarDisciplina(1L, 2L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveRemoverDisciplinaAssociada() {
        Disciplina disciplina = Disciplina.builder().id(2L).nome("Direito Administrativo").build();
        Concurso concurso = Concurso.builder().id(1L).nome("Concurso X")
                .disciplinas(new ArrayList<>(List.of(disciplina))).build();

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(disciplinaRepository.findById(2L)).thenReturn(Optional.of(disciplina));
        when(concursoMapper.toResponse(concurso)).thenReturn(
                ConcursoResponseDTO.builder().id(1L).nome("Concurso X").build());

        concursoService.removerDisciplina(1L, 2L);

        assertThat(concurso.getDisciplinas()).isEmpty();
    }

    @Test
    void deveLancarExcecaoAoRemoverDisciplinaNaoAssociada() {
        Disciplina disciplina = Disciplina.builder().id(2L).nome("Direito Administrativo").build();
        Concurso concurso = Concurso.builder().id(1L).nome("Concurso X").disciplinas(new ArrayList<>()).build();

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(disciplinaRepository.findById(2L)).thenReturn(Optional.of(disciplina));

        assertThatThrownBy(() -> concursoService.removerDisciplina(1L, 2L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveListarApenasConcursosComQuestoesReais() {
        Concurso concurso = Concurso.builder().id(7L).nome("PM-PE 2024 (Prova Aplicada)").build();
        ConcursoResponseDTO response = ConcursoResponseDTO.builder().id(7L).nome("PM-PE 2024 (Prova Aplicada)").build();

        when(concursoRepository.findComQuestoesReais()).thenReturn(List.of(concurso));
        when(concursoMapper.toResponseList(List.of(concurso))).thenReturn(List.of(response));

        List<ConcursoResponseDTO> resultado = concursoService.listarProvasDisponiveis();

        assertThat(resultado).containsExactly(response);
    }
}
