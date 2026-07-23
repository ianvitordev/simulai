package SimulaI.controller;

import java.util.List;

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

import SimulaI.dto.AssuntoRequestDTO;
import SimulaI.dto.AssuntoResponseDTO;
import SimulaI.service.AssuntoService;

@Tag(name = "Assuntos", description = "Cadastro de assuntos vinculados a uma disciplina")
@RestController
@RequestMapping("/api/assuntos")
public class AssuntoController {

    private final AssuntoService assuntoService;

    public AssuntoController(AssuntoService assuntoService) {
        this.assuntoService = assuntoService;
    }

    @Operation(summary = "Cadastrar um novo assunto")
    @PostMapping
    public ResponseEntity<AssuntoResponseDTO> cadastrar(@Valid @RequestBody AssuntoRequestDTO request) {
        AssuntoResponseDTO response = assuntoService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar um assunto pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<AssuntoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(assuntoService.buscarPorId(id));
    }

    @Operation(summary = "Listar todos os assuntos")
    @GetMapping
    public ResponseEntity<List<AssuntoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(assuntoService.listarTodos());
    }

    @Operation(summary = "Listar os assuntos de uma disciplina")
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<AssuntoResponseDTO>> listarPorDisciplina(@PathVariable Long disciplinaId) {
        return ResponseEntity.ok(assuntoService.listarPorDisciplina(disciplinaId));
    }

    @Operation(summary = "Atualizar os dados de um assunto")
    @PutMapping("/{id}")
    public ResponseEntity<AssuntoResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody AssuntoRequestDTO request) {
        return ResponseEntity.ok(assuntoService.atualizar(id, request));
    }

    @Operation(summary = "Remover um assunto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        assuntoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
