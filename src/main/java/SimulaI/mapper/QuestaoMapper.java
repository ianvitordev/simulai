package SimulaI.mapper;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import SimulaI.dto.QuestaoAdminResponseDTO;
import SimulaI.dto.QuestaoRequestDTO;
import SimulaI.dto.QuestaoResponseDTO;
import SimulaI.entity.Questao;

@Mapper(config = CentralMapperConfig.class, uses = AlternativaMapper.class)
public interface QuestaoMapper {

    /**
     * DTO de entrada → entidade nova.
     * Os relacionamentos (concurso, disciplina, assunto, usuario) chegam como IDs no DTO e
     * são resolvidos no Service a partir dos repositories, por isso são ignorados aqui.
     * As alternativas são convertidas pelo {@link AlternativaMapper}.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    @Mapping(target = "assunto", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Questao toEntity(QuestaoRequestDTO request);

    /**
     * Religa cada alternativa à sua questão dona, garantindo a consistência do relacionamento
     * bidirecional (necessário para cascade/orphanRemoval funcionarem na persistência).
     */
    @AfterMapping
    default void vincularAlternativas(@MappingTarget Questao questao) {
        if (questao.getAlternativas() != null) {
            questao.getAlternativas().forEach(alternativa -> alternativa.setQuestao(questao));
        }
    }

    /**
     * Entidade → DTO de saída. Relacionamentos são achatados para o nome legível
     * (null-safe: gerado com verificação de nulo quando o relacionamento for opcional).
     */
    @Mapping(target = "concurso", source = "concurso.nome")
    @Mapping(target = "disciplina", source = "disciplina.nome")
    @Mapping(target = "assunto", source = "assunto.nome")
    QuestaoResponseDTO toResponse(Questao questao);

    List<QuestaoResponseDTO> toResponseList(List<Questao> questoes);

    /**
     * Variante para moderação (ADMIN): as alternativas revelam qual é a correta. Nunca
     * deve ser usada em endpoints acessíveis a alunos.
     */
    @Mapping(target = "concurso", source = "concurso.nome")
    @Mapping(target = "disciplina", source = "disciplina.nome")
    @Mapping(target = "assunto", source = "assunto.nome")
    QuestaoAdminResponseDTO toAdminResponse(Questao questao);

    List<QuestaoAdminResponseDTO> toAdminResponseList(List<Questao> questoes);

    /**
     * Atualiza apenas os campos escalares. Relacionamentos e alternativas são tratados
     * no Service: os primeiros exigem resolução via repository, e as alternativas exigem
     * manipulação da coleção gerenciada pelo Hibernate para respeitar o orphanRemoval.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    @Mapping(target = "assunto", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    void updateEntityFromDto(QuestaoRequestDTO request, @MappingTarget Questao questao);
}
