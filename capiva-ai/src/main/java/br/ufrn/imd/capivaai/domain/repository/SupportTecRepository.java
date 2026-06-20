package br.ufrn.imd.capivaai.domain.repository;

import java.util.List;

public interface SupportTecRepository {

    void add(List<String> information);
    List<String> findClosestMatches(String query, int numberOfMatches);
    String findCloseMatch(String query);
}
