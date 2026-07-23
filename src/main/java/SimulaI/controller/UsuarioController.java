package SimulaI.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import SimulaI.dto.AlterarRoleRequestDTO;
import SimulaI.dto.UsuarioRequestDTO;
import SimulaI.dto.UsuarioResponseDTO;
import SimulaI.service.UsuarioService;

/**
 * Listar todos e buscar por email são restritos a ADMIN via SecurityConfig (regra de
 * matcher, não precisa de @PreAuthorize aqui). Buscar/atualizar/remover por id são
 * auto-serviço: o próprio usuário ou um ADMIN.
 */
@Tag(name = "Usuários", description = "Cadastro de usuários (alunos)")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cadastrar um novo usuário")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar um usuário pelo id")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.usuarioId")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @Operation(summary = "Buscar um usuário pelo email (apenas ADMIN)")
    @GetMapping("/email")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @Operation(summary = "Listar todos os usuários (apenas ADMIN)")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @Operation(summary = "Atualizar os dados de um usuário")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.usuarioId")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @Operation(summary = "Remover um usuário")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.usuarioId")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Promover/rebaixar um usuário entre ALUNO e ADMIN (apenas ADMIN)")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UsuarioResponseDTO> alterarRole(@PathVariable Long id,
                                                            @Valid @RequestBody AlterarRoleRequestDTO request) {
        return ResponseEntity.ok(usuarioService.alterarRole(id, request));
    }
}
