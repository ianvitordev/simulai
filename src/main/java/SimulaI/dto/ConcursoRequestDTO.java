package SimulaI.dto;

import SimulaI.enums.StatusConcurso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcursoRequestDTO {
    
      private String nome;

    private String orgao;

    private String cargo;

    private Integer ano;

    private Long bancaId;

    private StatusConcurso status;

    /** Opcional: URL do PDF do edital, usada por POST /concursos/{id}/edital/indexar (RAG). */
    private String editalUrl;

}
