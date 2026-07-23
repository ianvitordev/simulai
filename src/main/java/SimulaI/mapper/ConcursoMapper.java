package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import SimulaI.dto.ConcursoRequestDTO;
import SimulaI.dto.ConcursoResponseDTO;
import SimulaI.entity.Concurso;
import SimulaI.entity.Disciplina;

@Mapper(config = CentralMapperConfig.class)
public interface ConcursoMapper {

    /**
     * DTO de entrada → entidade nova.
     * A {@code banca} é resolvida no Service a partir de {@code bancaId}; as {@code disciplinas}
     * são gerenciadas por operação própria. Ambos ficam fora do mapeamento automático.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "disciplinas", ignore = true)
    Concurso toEntity(ConcursoRequestDTO request);

    /**
     * Entidade → DTO de saída. A banca é achatada para o nome legível.
     */
    @Mapping(target = "banca", source = "banca.nome")
    ConcursoResponseDTO toResponse(Concurso concurso);

    List<ConcursoResponseDTO> toResponseList(List<Concurso> concursos);

    /**
     * Atualização parcial dos dados cadastrais. Relacionamentos e id são preservados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "disciplinas", ignore = true)
    void updateEntityFromDto(ConcursoRequestDTO request, @MappingTarget Concurso concurso);

    /** Usado pelo MapStruct para converter List&lt;Disciplina&gt; em List&lt;String&gt; (nomes). */
    default String map(Disciplina disciplina) {
        return disciplina.getNome();
    }
}
