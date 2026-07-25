package SimulaI.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import SimulaI.dto.EstatisticaResponseDTO;
import SimulaI.service.EstatisticaService;

/**
 * Ownership direto por usuarioId (não precisa de um AuthorizationService dedicado, como
 * o de Simulado — o id no path já É o id do usuário, dá pra comparar direto com o
 * principal autenticado).
 */
@Tag(name = "Estatísticas", description = "Desempenho agregado do aluno por disciplina/assunto e evolução no tempo")
@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticaController {

    private final EstatisticaService estatisticaService;

    public EstatisticaController(EstatisticaService estatisticaService) {
        this.estatisticaService = estatisticaService;
    }

    @Operation(summary = "Obter as estatísticas completas de desempenho de um usuário")
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == authentication.principal.usuarioId")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<EstatisticaResponseDTO> obter(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(estatisticaService.obterEstatisticas(usuarioId));
    }
}
