package br.ufrn.imd.capivaai.module.repository;

import br.ufrn.imd.capivaai.domain.repository.SupportTecRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SupportTecRepositoryImpl implements SupportTecRepository {

    private final VectorStore vectorStore;

    public SupportTecRepositoryImpl(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    @Override
    public void add(List<String> information) {
        List<Document> documents = information.stream().map(Document::new).toList();
        vectorStore.add(documents);
    }

    @Override
    public List<String> findClosestMatches(String query, int numberOfMatches) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(numberOfMatches)
                .build();

        List<Document> result = vectorStore.similaritySearch(request);
        return  result.stream()
                .map(Document::getText)
                .toList();
    }

    @Override
    public String findCloseMatch(String query) {
        return findClosestMatches(query, 1).getFirst();
    }
}
