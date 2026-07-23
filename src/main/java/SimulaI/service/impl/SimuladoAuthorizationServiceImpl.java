package SimulaI.service.impl;

import org.springframework.stereotype.Service;

import SimulaI.repository.SimuladoRepository;
import SimulaI.service.SimuladoAuthorizationService;

@Service
public class SimuladoAuthorizationServiceImpl implements SimuladoAuthorizationService {

    private final SimuladoRepository simuladoRepository;

    public SimuladoAuthorizationServiceImpl(SimuladoRepository simuladoRepository) {
        this.simuladoRepository = simuladoRepository;
    }

    @Override
    public boolean isOwner(Long simuladoId, Long usuarioId) {
        return simuladoRepository.findById(simuladoId)
                .map(simulado -> simulado.getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }
}
