package SimulaI.service;

import java.util.List;

import SimulaI.dto.AlterarRoleRequestDTO;
import SimulaI.dto.UsuarioRequestDTO;
import SimulaI.dto.UsuarioResponseDTO;

public interface UsuarioService {

 UsuarioResponseDTO cadastrar(UsuarioRequestDTO request);

    UsuarioResponseDTO buscarPorId(Long id);

    UsuarioResponseDTO buscarPorEmail(String email);

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO request);

    void deletar(Long id);

    /**
     * Único mecanismo (fora do bootstrap inicial) para promover/rebaixar um usuário
     * entre ALUNO e ADMIN — só ADMIN pode chamar (aplicado no Controller).
     */
    UsuarioResponseDTO alterarRole(Long id, AlterarRoleRequestDTO request);
}
