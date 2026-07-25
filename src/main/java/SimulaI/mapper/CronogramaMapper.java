package SimulaI.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import SimulaI.dto.CronogramaResponseDTO;
import SimulaI.dto.ItemCronogramaResponseDTO;
import SimulaI.entity.Cronograma;
import SimulaI.entity.ItemCronograma;

/**
 * Só mapeia entidade → DTO: diferente de Simulado/Questao, o Cronograma nunca é
 * construído a partir de um DTO de entrada (os itens vêm da IA e são resolvidos/montados
 * manualmente no Service, com as entidades Disciplina/Assunto já em mãos).
 */
@Mapper(config = CentralMapperConfig.class)
public interface CronogramaMapper {

    CronogramaResponseDTO toResponse(Cronograma cronograma);

    @Mapping(target = "disciplina", source = "disciplina.nome")
    @Mapping(target = "assunto", source = "assunto.nome")
    ItemCronogramaResponseDTO toItemResponse(ItemCronograma item);
}
