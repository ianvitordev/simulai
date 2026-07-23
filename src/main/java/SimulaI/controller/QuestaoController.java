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

import SimulaI.dto.GerarQuestaoIARequestDTO;
import SimulaI.dto.QuestaoAdminResponseDTO;
import SimulaI.dto.QuestaoRequestDTO;
import SimulaI.dto.QuestaoResponseDTO;
import SimulaI.enums.Dificuldade;
import SimulaI.service.QuestaoService;

/**
 * Não expõe POST de criação manual: o único jeito de criar questões é via IA
 * (/gerar-ia, restrito a ADMIN no SecurityConfig). Este controller cobre consulta e
 * moderação (correção/remoção) de conteúdo já gerado.
 */
@Tag(name = "Questões", description = "Geração via IA, consulta e moderação de questões")
@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService) {
        this.questaoService = questaoService;
    }

    @Operation(summary = "Gerar novas questões via IA (único mecanismo de criação de questões)")
    @PostMapping("/gerar-ia")
    public ResponseEntity<List<QuestaoResponseDTO>> gerarViaIA(@Valid @RequestBody GerarQuestaoIARequestDTO request) {
        List<QuestaoResponseDTO> geradas = questaoService.gerarViaIA(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(geradas);
    }

    @Operation(summary = "Buscar uma questão pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<QuestaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(questaoService.buscarPorId(id));
    }

    @Operation(summary = "Listar todas as questões")
    @GetMapping
    public ResponseEntity<List<QuestaoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(questaoService.listarTodas());
    }

    @Operation(summary = "Listar todas as questões para moderação (ADMIN, expõe o gabarito)")
    @GetMapping("/moderacao")
    public ResponseEntity<List<QuestaoAdminResponseDTO>> listarTodasParaModeracao() {
        return ResponseEntity.ok(questaoService.listarTodasParaModeracao());
    }

    @Operation(summary = "Listar questões de uma disciplina")
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<QuestaoResponseDTO>> listarPorDisciplina(@PathVariable Long disciplinaId) {
        return ResponseEntity.ok(questaoService.listarPorDisciplina(disciplinaId));
    }

    @Operation(summary = "Listar questões de um assunto")
    @GetMapping("/assunto/{assuntoId}")
    public ResponseEntity<List<QuestaoResponseDTO>> listarPorAssunto(@PathVariable Long assuntoId) {
        return ResponseEntity.ok(questaoService.listarPorAssunto(assuntoId));
    }

    @Operation(summary = "Listar questões por nível de dificuldade")
    @GetMapping("/dificuldade/{dificuldade}")
    public ResponseEntity<List<QuestaoResponseDTO>> listarPorDificuldade(@PathVariable Dificuldade dificuldade) {
        return ResponseEntity.ok(questaoService.listarPorDificuldade(dificuldade));
    }

    @Operation(summary = "Corrigir/moderar o conteúdo de uma questão já gerada")
    @PutMapping("/{id}")
    public ResponseEntity<QuestaoResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody QuestaoRequestDTO request) {
        return ResponseEntity.ok(questaoService.atualizar(id, request));
    }

    @Operation(summary = "Remover uma questão")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        questaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
