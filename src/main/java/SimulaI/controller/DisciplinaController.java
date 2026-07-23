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

import SimulaI.dto.DisciplinaRequestDTO;
import SimulaI.dto.DisciplinaResponseDTO;
import SimulaI.service.DisciplinaService;

@Tag(name = "Disciplinas", description = "Cadastro de disciplinas")
@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @Operation(summary = "Cadastrar uma nova disciplina")
    @PostMapping
    public ResponseEntity<DisciplinaResponseDTO> cadastrar(@Valid @RequestBody DisciplinaRequestDTO request) {
        DisciplinaResponseDTO response = disciplinaService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar uma disciplina pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(disciplinaService.buscarPorId(id));
    }

    @Operation(summary = "Listar todas as disciplinas")
    @GetMapping
    public ResponseEntity<List<DisciplinaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(disciplinaService.listarTodas());
    }

    @Operation(summary = "Atualizar os dados de uma disciplina")
    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaResponseDTO> atualizar(@PathVariable Long id,
                                                             @Valid @RequestBody DisciplinaRequestDTO request) {
        return ResponseEntity.ok(disciplinaService.atualizar(id, request));
    }

    @Operation(summary = "Remover uma disciplina")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        disciplinaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
