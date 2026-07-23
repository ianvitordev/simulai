package SimulaI.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Substitui o PgVectorStore (excluído em application.yml de teste — H2 não suporta a
 * extensão "vector" do Postgres) por um VectorStore em memória, só para o contexto de
 * teste conseguir subir com os beans que dependem de VectorStore (GeradorQuestaoIAImpl,
 * IndexadorConteudoIAImpl).
 */
@TestConfiguration
public class TestVectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
