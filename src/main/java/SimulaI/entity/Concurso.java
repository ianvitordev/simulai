package SimulaI.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import SimulaI.enums.StatusConcurso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "concursos")
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String orgao;

    @Column(nullable = false)
    private String cargo;

    @Column(nullable = true)
    private Integer ano;

    @Column
    private String editalUrl;

    /**
     * Indica se o conteúdo de editalUrl já foi baixado, fatiado e indexado no vector
     * store (etapa 10/RAG). Evita que a geração de questões precise consultar o vector
     * store só para descobrir se há contexto de edital disponível para este concurso.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean editalIndexado = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConcurso status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banca_id", nullable = false)
    private Banca banca;

    @ManyToMany
    @JoinTable(
    name = "concurso_disciplina",
    joinColumns = @JoinColumn(name = "concurso_id"),
    inverseJoinColumns = @JoinColumn(name = "disciplina_id")
)
private List<Disciplina> disciplinas;
}
