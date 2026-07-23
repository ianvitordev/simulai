package SimulaI.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuração central e única de todos os mappers MapStruct do projeto.
 *
 * <ul>
 *   <li>componentModel = "spring" → cada mapper vira um bean gerenciado, injetável por construtor.</li>
 *   <li>unmappedTargetPolicy = IGNORE → campos de destino não mapeados (ex.: campos com default
 *       na entidade, ou relacionamentos resolvidos no Service) não quebram o build.</li>
 *   <li>nullValuePropertyMappingStrategy = IGNORE → em atualizações parciais (@MappingTarget),
 *       valores nulos do DTO não sobrescrevem o que já existe na entidade.</li>
 * </ul>
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CentralMapperConfig {
}
