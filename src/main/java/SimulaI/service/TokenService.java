package SimulaI.service;

import SimulaI.entity.Usuario;

public interface TokenService {

    String gerarToken(Usuario usuario);

    boolean validarToken(String token);

    String extrairEmail(String token);

    Long extrairUsuarioId(String token);

    long getExpiracaoSegundos();
}
