package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import SimulaI.dto.AssuntoRequestDTO;
import SimulaI.dto.AssuntoResponseDTO;
import SimulaI.entity.Assunto;

@Mapper(config = CentralMapperConfig.class)
public interface AssuntoMapper {

    /**
     * A {@code disciplina} chega como {@code disciplinaId} e é resolvida no Service
     * via DisciplinaRepository, por isso é ignorada aqui.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    Assunto toEntity(AssuntoRequestDTO request);

    @Mapping(target = "disciplina", source = "disciplina.nome")
    AssuntoResponseDTO toResponse(Assunto assunto);

    List<AssuntoResponseDTO> toResponseList(List<Assunto> assuntos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    void updateEntityFromDto(AssuntoRequestDTO request, @MappingTarget Assunto assunto);
}
