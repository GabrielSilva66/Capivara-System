package br.ufrn.imd.capivaai.domain.repository;

import org.springframework.ai.document.Document;

import java.util.List;

public interface SupportTecRepository {

    void add(List<String> information);
    void addChunks(List<Document> chunks);
    List<String> findClosestMatches(String query, int numberOfMatches);
    String findCloseMatch(String query);
}
