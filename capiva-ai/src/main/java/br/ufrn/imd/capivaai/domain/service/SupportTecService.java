package br.ufrn.imd.capivaai.domain.service;

import br.ufrn.imd.capivaai.domain.model.AskResponse;
import br.ufrn.imd.capivaai.module.dto.request.AskInputDTO;

import java.util.List;

/**
 * Porta de domínio do módulo de Suporte Técnico com IA.
 * Casos de uso:
 *   Ingestão de conhecimento na VectorStore (base RAG).
 *   Busca semântica na base de conhecimento.
 *   Chat conversacional com RAG + memória de histórico.
 *
 */
public interface SupportTecService {

    void add(List<String> information);

    void ingestDocument(String fileUrl);

    List<String> findClosestMatches(String query, int numberOfMatches);

    String findCloseMatch(String query);

    AskResponse ask(AskInputDTO input);
}

