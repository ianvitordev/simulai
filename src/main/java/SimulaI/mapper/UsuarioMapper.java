package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import SimulaI.dto.UsuarioRequestDTO;
import SimulaI.dto.UsuarioResponseDTO;
import SimulaI.entity.Usuario;

@Mapper(config = CentralMapperConfig.class)
public interface UsuarioMapper {

    /**
     * DTO de entrada → entidade nova.
     * O {@code id} é gerado pelo banco e a {@code role} é definida pela regra de negócio
     * no Service (padrão ALUNO), por isso ambos são ignorados aqui.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    Usuario toEntity(UsuarioRequestDTO request);

    UsuarioResponseDTO toResponse(Usuario usuario);

    List<UsuarioResponseDTO> toResponseList(List<Usuario> usuarios);

    /**
     * Atualização parcial: aplica os campos não nulos do DTO sobre a entidade existente.
     * {@code id} e {@code role} nunca são alterados por atualização de dados cadastrais.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntityFromDto(UsuarioRequestDTO request, @MappingTarget Usuario usuario);
}
