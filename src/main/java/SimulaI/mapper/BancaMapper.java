package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import SimulaI.dto.BancaRequestDTO;
import SimulaI.dto.BancaResponseDTO;
import SimulaI.entity.Banca;

@Mapper(config = CentralMapperConfig.class)
public interface BancaMapper {

    @Mapping(target = "id", ignore = true)
    Banca toEntity(BancaRequestDTO request);

    BancaResponseDTO toResponse(Banca banca);

    List<BancaResponseDTO> toResponseList(List<Banca> bancas);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(BancaRequestDTO request, @MappingTarget Banca banca);
}
