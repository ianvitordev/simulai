package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import SimulaI.dto.DisciplinaRequestDTO;
import SimulaI.dto.DisciplinaResponseDTO;
import SimulaI.entity.Disciplina;

@Mapper(config = CentralMapperConfig.class)
public interface DisciplinaMapper {

    @Mapping(target = "id", ignore = true)
    Disciplina toEntity(DisciplinaRequestDTO request);

    DisciplinaResponseDTO toResponse(Disciplina disciplina);

    List<DisciplinaResponseDTO> toResponseList(List<Disciplina> disciplinas);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(DisciplinaRequestDTO request, @MappingTarget Disciplina disciplina);
}
