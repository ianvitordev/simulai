package SimulaI.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import SimulaI.dto.GerarSimuladoRequestDTO;
import SimulaI.dto.RespostaUsuarioRequestDTO;
import SimulaI.dto.RespostaUsuarioResponseDTO;
import SimulaI.dto.SimuladoResponseDTO;
import SimulaI.dto.SimuladoResultadoDTO;
import SimulaI.dto.SimuladoRevisaoDTO;
import SimulaI.service.SimuladoService;
import SimulaI.service.UsuarioDetailsImpl;

/**
 * Ownership de Simulado: id do simulado != id do usuário autenticado, então não dá para
 * expressar "é dono" comparando path variables diretamente — por isso as regras usam o
 * bean SimuladoAuthorizationService via @PreAuthorize (ADMIN sempre passa; aluno só acessa
 * o próprio simulado).
 */
@Tag(name = "Simulados", description = "Geração, execução e correção de simulados")
@RestController
@RequestMapping("/api/simulados")
public class SimuladoController {

    private final SimuladoService simuladoService;

    public SimuladoController(SimuladoService simuladoService) {
        this.simuladoService = simuladoService;
    }

    @Operation(summary = "Gerar um novo simulado sorteando questões do banco")
    @PostMapping("/gerar")
    public ResponseEntity<SimuladoResponseDTO> gerar(@AuthenticationPrincipal UsuarioDetailsImpl principal,
                                                       @Valid @RequestBody GerarSimuladoRequestDTO request) {
        SimuladoResponseDTO response = simuladoService.gerar(principal.getUsuarioId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar um simulado pelo id")
    @PreAuthorize("hasRole('ADMIN') or @simuladoAuthorizationServiceImpl.isOwner(#id, authentication.principal.usuarioId)")
    @GetMapping("/{id}")
    public ResponseEntity<SimuladoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(simuladoService.buscarPorId(id));
    }

    @Operation(summary = "Listar os simulados de um usuário")
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == authentication.principal.usuarioId")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<SimuladoResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(simuladoService.listarPorUsuario(usuarioId));
    }

    @Operation(summary = "Iniciar um simulado (CRIADO -> EM_ANDAMENTO)")
    @PreAuthorize("hasRole('ADMIN') or @simuladoAuthorizationServiceImpl.isOwner(#id, authentication.principal.usuarioId)")
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<SimuladoResponseDTO> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(simuladoService.iniciar(id));
    }

    @Operation(summary = "Responder (ou trocar a resposta de) uma questão do simulado")
    @PreAuthorize("hasRole('ADMIN') or @simuladoAuthorizationServiceImpl.isOwner(#id, authentication.principal.usuarioId)")
    @PostMapping("/{id}/respostas")
    public ResponseEntity<RespostaUsuarioResponseDTO> responderQuestao(
            @PathVariable Long id,
            @Valid @RequestBody RespostaUsuarioRequestDTO request) {
        return ResponseEntity.ok(simuladoService.responderQuestao(id, request));
    }

    @Operation(summary = "Finalizar o simulado e calcular o resultado (EM_ANDAMENTO -> FINALIZADO)")
    @PreAuthorize("hasRole('ADMIN') or @simuladoAuthorizationServiceImpl.isOwner(#id, authentication.principal.usuarioId)")
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<SimuladoResultadoDTO> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(simuladoService.finalizar(id));
    }

    @Operation(summary = "Cancelar um simulado não finalizado")
    @PreAuthorize("hasRole('ADMIN') or @simuladoAuthorizationServiceImpl.isOwner(#id, authentication.principal.usuarioId)")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SimuladoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(simuladoService.cancelar(id));
    }

    @Operation(summary = "Revisar um simulado finalizado (gabarito completo + resposta do usuário)")
    @PreAuthorize("hasRole('ADMIN') or @simuladoAuthorizationServiceImpl.isOwner(#id, authentication.principal.usuarioId)")
    @GetMapping("/{id}/revisao")
    public ResponseEntity<SimuladoRevisaoDTO> revisar(@PathVariable Long id) {
        return ResponseEntity.ok(simuladoService.revisar(id));
    }
}
