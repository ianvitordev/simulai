package SimulaI.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import SimulaI.dto.AlterarRoleRequestDTO;
import SimulaI.dto.UsuarioRequestDTO;
import SimulaI.dto.UsuarioResponseDTO;
import SimulaI.entity.Questao;
import SimulaI.entity.Usuario;
import SimulaI.enums.Role;
import SimulaI.enums.TipoCodigoVerificacao;
import SimulaI.exception.RecursoNaoEncontradoException;
import SimulaI.exception.RegistroDuplicadoException;
import SimulaI.mapper.UsuarioMapper;
import SimulaI.repository.CodigoVerificacaoRepository;
import SimulaI.repository.CronogramaRepository;
import SimulaI.repository.QuestaoRepository;
import SimulaI.repository.SimuladoRepository;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.CodigoVerificacaoService;
import SimulaI.service.UsuarioService;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final CodigoVerificacaoService codigoVerificacaoService;
    private final SimuladoRepository simuladoRepository;
    private final CronogramaRepository cronogramaRepository;
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final QuestaoRepository questaoRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                               UsuarioMapper usuarioMapper,
                               PasswordEncoder passwordEncoder,
                               CodigoVerificacaoService codigoVerificacaoService,
                               SimuladoRepository simuladoRepository,
                               CronogramaRepository cronogramaRepository,
                               CodigoVerificacaoRepository codigoVerificacaoRepository,
                               QuestaoRepository questaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.codigoVerificacaoService = codigoVerificacaoService;
        this.simuladoRepository = simuladoRepository;
        this.cronogramaRepository = cronogramaRepository;
        this.codigoVerificacaoRepository = codigoVerificacaoRepository;
        this.questaoRepository = questaoRepository;
    }

    @Override
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RegistroDuplicadoException("Já existe um usuário cadastrado com o email: " + request.getEmail());
        }

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setRole(Role.ALUNO);
        usuario.setEmailVerificado(false);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        codigoVerificacaoService.gerarEEnviar(usuarioSalvo, TipoCodigoVerificacao.CONFIRMACAO_CADASTRO);

        return usuarioMapper.toResponse(usuarioSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com email: " + email));
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioMapper.toResponseList(usuarioRepository.findAll());
    }

    @Override
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = buscarEntidadePorId(id);

        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RegistroDuplicadoException("Já existe um usuário cadastrado com o email: " + request.getEmail());
        }

        usuarioMapper.updateEntityFromDto(request, usuario);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        return usuarioMapper.toResponse(usuario);
    }

    /**
     * Remoção em cascata: simulados/respostas (cascade já existe em Simulado), cronograma
     * e códigos de verificação pertencem só a este usuário, então são apagados junto.
     * Questões que ele gerou via IA são só desvinculadas (usuario=null) — nunca apagadas,
     * porque podem ter sido usadas em simulados de OUTROS alunos (apagar arrastaria o
     * histórico deles também).
     */
    @Override
    public void deletar(Long id) {
        Usuario usuario = buscarEntidadePorId(id);

        List<Questao> questoesGeradas = questaoRepository.findByUsuario(usuario);
        questoesGeradas.forEach(questao -> questao.setUsuario(null));

        codigoVerificacaoRepository.deleteAll(codigoVerificacaoRepository.findByUsuario(usuario));
        cronogramaRepository.findByUsuario(usuario).ifPresent(cronogramaRepository::delete);
        simuladoRepository.deleteAll(simuladoRepository.findByUsuario(usuario));

        usuarioRepository.delete(usuario);
    }

    @Override
    public UsuarioResponseDTO alterarRole(Long id, AlterarRoleRequestDTO request) {
        Usuario usuario = buscarEntidadePorId(id);
        usuario.setRole(request.getRole());
        return usuarioMapper.toResponse(usuario);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Usuário", id));
    }
}
