package SimulaI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import SimulaI.entity.CodigoVerificacao;
import SimulaI.entity.Usuario;
import SimulaI.enums.TipoCodigoVerificacao;

public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {

    List<CodigoVerificacao> findByUsuarioAndTipoAndUsadoFalse(Usuario usuario, TipoCodigoVerificacao tipo);

    /** Usada ao remover um usuário — apaga todos os códigos (usados ou não), não só os pendentes. */
    List<CodigoVerificacao> findByUsuario(Usuario usuario);

    Optional<CodigoVerificacao> findFirstByUsuarioAndTipoAndUsadoFalseOrderByCriadoEmDesc(
            Usuario usuario, TipoCodigoVerificacao tipo);
}
