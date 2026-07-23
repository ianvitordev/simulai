package SimulaI.service.ia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import SimulaI.entity.Concurso;
import SimulaI.entity.Questao;
import SimulaI.exception.GeracaoIAException;
import SimulaI.exception.RegraNegocioException;

@Service
public class IndexadorConteudoIAImpl implements IndexadorConteudoIA {

    private final VectorStore vectorStore;
    private final WebClient webClient;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    public IndexadorConteudoIAImpl(VectorStore vectorStore, WebClient.Builder webClientBuilder) {
        this.vectorStore = vectorStore;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void indexarQuestao(Questao questao) {
        String conteudo = "Enunciado: %s%nExplicação: %s"
                .formatted(questao.getEnunciado(), questao.getExplicacao() != null ? questao.getExplicacao() : "");

        Document documento = Document.builder()
                .text(conteudo)
                .metadata(Map.of(
                        "tipo", "questao_historica",
                        "questaoId", questao.getId(),
                        "disciplinaId", questao.getDisciplina().getId(),
                        "assuntoId", questao.getAssunto().getId()))
                .build();

        vectorStore.add(List.of(documento));
    }

    @Override
    public int indexarEdital(Concurso concurso) {
        if (concurso.getEditalUrl() == null || concurso.getEditalUrl().isBlank()) {
            throw new RegraNegocioException("Este concurso não possui uma URL de edital cadastrada.");
        }

        byte[] conteudoPdf = baixarPdf(concurso.getEditalUrl());
        Resource recurso = new ByteArrayResource(conteudoPdf);

        List<Document> documentosBrutos = new TikaDocumentReader(recurso).get();

        List<Document> documentosComMetadados = documentosBrutos.stream()
                .map(documento -> {
                    Map<String, Object> metadados = new HashMap<>(documento.getMetadata());
                    metadados.put("tipo", "edital");
                    metadados.put("concursoId", concurso.getId());
                    return Document.builder().text(documento.getText()).metadata(metadados).build();
                })
                .toList();

        List<Document> chunks = textSplitter.split(documentosComMetadados);
        vectorStore.add(chunks);

        return chunks.size();
    }

    private byte[] baixarPdf(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception e) {
            throw new GeracaoIAException("Falha ao baixar o edital em " + url + ": " + e.getMessage(), e);
        }
    }
}
