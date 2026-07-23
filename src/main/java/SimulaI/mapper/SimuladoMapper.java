package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import SimulaI.dto.SimuladoResponseDTO;
import SimulaI.entity.Simulado;

@Mapper(config = CentralMapperConfig.class, uses = QuestaoMapper.class)
public interface SimuladoMapper {

    @Mapping(target = "usuario", source = "usuario.nome")
    @Mapping(target = "concurso", source = "concurso.nome")
    SimuladoResponseDTO toResponse(Simulado simulado);

    List<SimuladoResponseDTO> toResponseList(List<Simulado> simulados);
}
