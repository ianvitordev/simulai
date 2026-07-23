package SimulaI.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

import SimulaI.enums.Dificuldade;
import SimulaI.enums.TipoQuestao;

@Entity
@Table(name = "questoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(columnDefinition = "TEXT")
    private String explicacao;

    @Column(nullable = false)
    private Integer ano;

    @Column(length = 200)
    private String fonte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Dificuldade dificuldade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoQuestao tipo;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean geradaPorIA = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativa = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantidadeAplicacoes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer tempoMedioSegundos = 0;

    @Builder.Default
    @Column(nullable = false)
    private Double percentualAcertos = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_id")
    private Concurso concurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(
            mappedBy = "questao",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Alternativa> alternativas;

}