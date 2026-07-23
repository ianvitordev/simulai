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

import SimulaI.dto.BancaRequestDTO;
import SimulaI.dto.BancaResponseDTO;
import SimulaI.service.BancaService;

@Tag(name = "Bancas", description = "Cadastro de bancas organizadoras de concursos")
@RestController
@RequestMapping("/api/bancas")
public class BancaController {

    private final BancaService bancaService;

    public BancaController(BancaService bancaService) {
        this.bancaService = bancaService;
    }

    @Operation(summary = "Cadastrar uma nova banca")
    @PostMapping
    public ResponseEntity<BancaResponseDTO> cadastrar(@Valid @RequestBody BancaRequestDTO request) {
        BancaResponseDTO response = bancaService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar uma banca pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<BancaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bancaService.buscarPorId(id));
    }

    @Operation(summary = "Listar todas as bancas")
    @GetMapping
    public ResponseEntity<List<BancaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(bancaService.listarTodas());
    }

    @Operation(summary = "Atualizar os dados de uma banca")
    @PutMapping("/{id}")
    public ResponseEntity<BancaResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody BancaRequestDTO request) {
        return ResponseEntity.ok(bancaService.atualizar(id, request));
    }

    @Operation(summary = "Remover uma banca")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        bancaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
