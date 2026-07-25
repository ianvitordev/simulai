package SimulaI.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import SimulaI.dto.CronogramaResponseDTO;
import SimulaI.dto.GerarCronogramaRequestDTO;
import SimulaI.service.CronogramaService;

/**
 * Ownership direto por usuarioId, mesmo padrão do EstatisticaController — é ação de
 * autoatendimento do aluno (não é curadoria de catálogo), por isso não entra nas regras
 * ADMIN-only do SecurityConfig, só o @PreAuthorize aqui.
 */
@Tag(name = "Cronogramas", description = "Cronograma de estudos gerado por IA a partir do desempenho do aluno")
@RestController
@RequestMapping("/api/cronogramas")
public class CronogramaController {

    private final CronogramaService cronogramaService;

    public CronogramaController(CronogramaService cronogramaService) {
        this.cronogramaService = cronogramaService;
    }

    @Operation(summary = "Gerar um novo cronograma de estudos via IA (substitui o anterior, se existir)")
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == authentication.principal.usuarioId")
    @PostMapping("/usuario/{usuarioId}/gerar")
    public ResponseEntity<CronogramaResponseDTO> gerar(@PathVariable Long usuarioId,
                                                         @Valid @RequestBody GerarCronogramaRequestDTO request) {
        CronogramaResponseDTO response = cronogramaService.gerar(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obter o cronograma de estudos atual de um usuário")
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == authentication.principal.usuarioId")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CronogramaResponseDTO> obterAtual(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(cronogramaService.obterAtual(usuarioId));
    }
}
