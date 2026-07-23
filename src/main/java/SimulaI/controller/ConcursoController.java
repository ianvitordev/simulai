package SimulaI.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import SimulaI.dto.ConcursoRequestDTO;
import SimulaI.dto.ConcursoResponseDTO;
import SimulaI.service.ConcursoService;

@Tag(name = "Concursos", description = "Cadastro de concursos e gestão das disciplinas do edital")
@RestController
@RequestMapping("/api/concursos")
public class ConcursoController {

    private final ConcursoService concursoService;

    public ConcursoController(ConcursoService concursoService) {
        this.concursoService = concursoService;
    }

    @Operation(summary = "Cadastrar um novo concurso")
    @PostMapping
    public ResponseEntity<ConcursoResponseDTO> cadastrar(@Valid @RequestBody ConcursoRequestDTO request) {
        ConcursoResponseDTO response = concursoService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar um concurso pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<ConcursoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(concursoService.buscarPorId(id));
    }

    @Operation(summary = "Listar todos os concursos")
    @GetMapping
    public ResponseEntity<List<ConcursoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(concursoService.listarTodos());
    }

    @Operation(summary = "Atualizar os dados de um concurso")
    @PutMapping("/{id}")
    public ResponseEntity<ConcursoResponseDTO> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody ConcursoRequestDTO request) {
        return ResponseEntity.ok(concursoService.atualizar(id, request));
    }

    @Operation(summary = "Remover um concurso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        concursoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Associar uma disciplina ao edital do concurso")
    @PostMapping("/{concursoId}/disciplinas/{disciplinaId}")
    public ResponseEntity<ConcursoResponseDTO> adicionarDisciplina(@PathVariable Long concursoId,
                                                                     @PathVariable Long disciplinaId) {
        return ResponseEntity.ok(concursoService.adicionarDisciplina(concursoId, disciplinaId));
    }

    @Operation(summary = "Remover uma disciplina do edital do concurso")
    @DeleteMapping("/{concursoId}/disciplinas/{disciplinaId}")
    public ResponseEntity<ConcursoResponseDTO> removerDisciplina(@PathVariable Long concursoId,
                                                                   @PathVariable Long disciplinaId) {
        return ResponseEntity.ok(concursoService.removerDisciplina(concursoId, disciplinaId));
    }

    @Operation(summary = "Baixar e indexar o edital do concurso para uso como contexto de geração de questões (RAG)")
    @PostMapping("/{id}/edital/indexar")
    public ResponseEntity<Map<String, Integer>> indexarEdital(@PathVariable Long id) {
        int chunksIndexados = concursoService.indexarEdital(id);
        return ResponseEntity.ok(Map.of("chunksIndexados", chunksIndexados));
    }
}
