package SimulaI.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import SimulaI.entity.Usuario;
import SimulaI.repository.UsuarioRepository;
import SimulaI.service.UsuarioDetailsImpl;

@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lança UsernameNotFoundException (contrato do Spring Security) propositalmente,
     * não uma exceção personalizada: o DaoAuthenticationProvider a captura e converte
     * em BadCredentialsException genérica, evitando enumeração de emails cadastrados.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));
        return new UsuarioDetailsImpl(usuario);
    }
}
