package SimulaI.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import SimulaI.dto.AlternativaAdminResponseDTO;
import SimulaI.dto.AlternativaRequestDTO;
import SimulaI.dto.AlternativaResponseDTO;
import SimulaI.entity.Alternativa;

@Mapper(config = CentralMapperConfig.class)
public interface AlternativaMapper {

    /**
     * O relacionamento {@code questao} é atribuído pelo lado dono (QuestaoMapper/Service),
     * portanto é ignorado aqui para evitar referência circular no mapeamento.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    Alternativa toEntity(AlternativaRequestDTO request);

    List<Alternativa> toEntityList(List<AlternativaRequestDTO> requests);

    /**
     * O campo {@code correta} é deliberadamente omitido do response para não expor o gabarito
     * ao cliente ao renderizar a questão. Endpoints de moderação (ADMIN) usam
     * {@link #toAdminResponse(Alternativa)} em vez deste método.
     */
    AlternativaResponseDTO toResponse(Alternativa alternativa);

    List<AlternativaResponseDTO> toResponseList(List<Alternativa> alternativas);

    /**
     * Variante para moderação (ADMIN): inclui {@code correta}. Nunca deve ser usada em
     * endpoints acessíveis a alunos.
     */
    AlternativaAdminResponseDTO toAdminResponse(Alternativa alternativa);

    List<AlternativaAdminResponseDTO> toAdminResponseList(List<Alternativa> alternativas);
}
