package SimulaI.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.CronogramaResponseDTO;
import SimulaI.dto.EstatisticaResponseDTO;
import SimulaI.dto.GerarCronogramaRequestDTO;
import SimulaI.entity.Assunto;
import SimulaI.entity.Cronograma;
import SimulaI.entity.ItemCronograma;
import SimulaI.entity.Usuario;
import SimulaI.enums.DiaSemana;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegraNegocioException;
import SimulaI.mapper.CronogramaMapper;
import SimulaI.repository.AssuntoRepository;
import SimulaI.repository.CronogramaRepository;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.CronogramaService;
import SimulaI.service.EstatisticaService;
import SimulaI.service.ia.CronogramaGeradoIA;
import SimulaI.service.ia.GeradorCronogramaIA;
import SimulaI.service.ia.ItemCronogramaGeradoIA;
import SimulaI.service.ia.OpcaoEstudoIA;

@Service
@Transactional
public class CronogramaServiceImpl implements CronogramaService {

    private final CronogramaRepository cronogramaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AssuntoRepository assuntoRepository;
    private final EstatisticaService estatisticaService;
    private final GeradorCronogramaIA geradorCronogramaIA;
    private final CronogramaMapper cronogramaMapper;

    public CronogramaServiceImpl(CronogramaRepository cronogramaRepository,
                                  UsuarioRepository usuarioRepository,
                                  AssuntoRepository assuntoRepository,
                                  EstatisticaService estatisticaService,
                                  GeradorCronogramaIA geradorCronogramaIA,
                                  CronogramaMapper cronogramaMapper) {
        this.cronogramaRepository = cronogramaRepository;
        this.usuarioRepository = usuarioRepository;
        this.assuntoRepository = assuntoRepository;
        this.estatisticaService = estatisticaService;
        this.geradorCronogramaIA = geradorCronogramaIA;
        this.cronogramaMapper = cronogramaMapper;
    }

    @Override
    public CronogramaResponseDTO gerar(Long usuarioId, GerarCronogramaRequestDTO request) {
        Usuario usuario = buscarUsuario(usuarioId);

        List<Assunto> catalogo = assuntoRepository.findAll();
        if (catalogo.isEmpty()) {
            throw new RegraNegocioException(
                    "Não há disciplinas/assuntos cadastrados no catálogo para montar um cronograma.");
        }

        EstatisticaResponseDTO estatisticas = estatisticaService.obterEstatisticas(usuarioId);
        Map<String, double[]> desempenhoPorChave = indexarDesempenho(estatisticas);

        List<OpcaoEstudoIA> opcoes = catalogo.stream()
                .map(assunto -> montarOpcao(assunto, desempenhoPorChave))
                .toList();

        CronogramaGeradoIA gerado = geradorCronogramaIA.gerar(opcoes, request.getDiasPorSemana(),
                request.getHorasPorDia());

        Map<String, Assunto> assuntoPorChave = catalogo.stream()
                .collect(Collectors.toMap(assunto -> chave(assunto.getDisciplina().getNome(), assunto.getNome()),
                        assunto -> assunto, (a, b) -> a));

        cronogramaRepository.findByUsuario(usuario).ifPresent(anterior -> {
            cronogramaRepository.delete(anterior);
            cronogramaRepository.flush();
        });

        Cronograma cronograma = Cronograma.builder()
                .usuario(usuario)
                .diasPorSemana(request.getDiasPorSemana())
                .horasPorDia(request.getHorasPorDia())
                .observacaoGeral(gerado.observacaoGeral())
                .itens(new ArrayList<>())
                .build();

        List<ItemCronograma> itens = gerado.itens().stream()
                .map(itemGerado -> montarItem(itemGerado, assuntoPorChave, cronograma))
                .collect(Collectors.toCollection(ArrayList::new));
        cronograma.setItens(itens);

        Cronograma salvo = cronogramaRepository.save(cronograma);
        return cronogramaMapper.toResponse(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public CronogramaResponseDTO obterAtual(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Cronograma cronograma = cronogramaRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Você ainda não gerou nenhum cronograma de estudos."));
        return cronogramaMapper.toResponse(cronograma);
    }

    /**
     * disciplina/assunto da IA sempre batem com alguma opção da lista informada — já
     * validado em GeradorCronogramaIAImpl.validarFormato antes de chegar aqui.
     */
    private ItemCronograma montarItem(ItemCronogramaGeradoIA itemGerado, Map<String, Assunto> assuntoPorChave,
                                       Cronograma cronograma) {
        Assunto assunto = assuntoPorChave.get(chave(itemGerado.disciplina(), itemGerado.assunto()));

        return ItemCronograma.builder()
                .cronograma(cronograma)
                .diaSemana(DiaSemana.fromTextoLivre(itemGerado.diaSemana()))
                .disciplina(assunto.getDisciplina())
                .assunto(assunto)
                .duracaoMinutos(itemGerado.duracaoMinutos())
                .foco(itemGerado.foco())
                .justificativa(itemGerado.justificativa())
                .build();
    }

    private OpcaoEstudoIA montarOpcao(Assunto assunto, Map<String, double[]> desempenhoPorChave) {
        double[] desempenho = desempenhoPorChave.get(chave(assunto.getDisciplina().getNome(), assunto.getNome()));
        int totalRespondidas = desempenho == null ? 0 : (int) desempenho[1];
        double percentual = desempenho == null ? 0.0 : desempenho[0];

        return new OpcaoEstudoIA(assunto.getDisciplina().getNome(), assunto.getNome(), percentual, totalRespondidas);
    }

    /**
     * Chave "disciplina|assunto" -> [percentual, totalRespondidas], a partir da
     * estatística já agregada por EstatisticaService (evita repetir a agregação aqui).
     */
    private Map<String, double[]> indexarDesempenho(EstatisticaResponseDTO estatisticas) {
        Map<String, double[]> indice = new HashMap<>();
        estatisticas.getPorDisciplina().forEach(disciplina ->
                disciplina.getPorAssunto().forEach(assunto ->
                        indice.put(chave(disciplina.getDisciplina(), assunto.getAssunto()),
                                new double[] {assunto.getPercentual(), assunto.getTotalRespondidas()})));
        return indice;
    }

    private String chave(String disciplina, String assunto) {
        String disciplinaNormalizada = disciplina == null ? "" : disciplina.trim().toLowerCase(Locale.ROOT);
        String assuntoNormalizado = assunto == null ? "" : assunto.trim().toLowerCase(Locale.ROOT);
        return disciplinaNormalizada + "|" + assuntoNormalizado;
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Usuário", usuarioId));
    }
}
