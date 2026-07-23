package SimulaI.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import SimulaI.dto.RespostaUsuarioResponseDTO;
import SimulaI.entity.RespostaUsuario;

@Mapper(config = CentralMapperConfig.class)
public interface RespostaUsuarioMapper {

    @Mapping(target = "questaoId", source = "questao.id")
    @Mapping(target = "alternativaMarcada", source = "alternativaMarcada.letra")
    RespostaUsuarioResponseDTO toResponse(RespostaUsuario respostaUsuario);
}
