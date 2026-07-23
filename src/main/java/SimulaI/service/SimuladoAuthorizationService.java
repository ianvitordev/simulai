package SimulaI.service;

public interface SimuladoAuthorizationService {

    /**
     * Usado em expressões @PreAuthorize dos endpoints de Simulado, já que a ownership
     * não pode ser expressa comparando path variables diretamente (id do simulado
     * != id do usuário autenticado).
     */
    boolean isOwner(Long simuladoId, Long usuarioId);
}
