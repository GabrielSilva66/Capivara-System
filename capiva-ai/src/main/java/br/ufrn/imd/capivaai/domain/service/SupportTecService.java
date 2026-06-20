package br.ufrn.imd.capivaai.domain.service;

import java.util.List;

/**
 * Porta de domínio do módulo de Suporte Técnico com IA.
 * <p>
 * Implementa os casos de uso:
 * <ul>
 *   <li>Ingestão de conhecimento na VectorStore (base RAG).</li>
 *   <li>Busca semântica na base de conhecimento.</li>
 *   <li>Chat conversacional com RAG + memória de histórico.</li>
 * </ul>
 */
public interface SupportTecService {

    /**
     * Ingere uma lista de textos na VectorStore.
     *
     * @param information textos a serem indexados
     */
    void add(List<String> information);

    /**
     * Recupera os {@code numberOfMatches} trechos mais similares à {@code query}.
     *
     * @param query          texto de busca
     * @param numberOfMatches quantidade de resultados
     * @return lista de textos similares
     */
    List<String> findClosestMatches(String query, int numberOfMatches);

    /**
     * Recupera o trecho mais similar à {@code query}.
     *
     * @param query texto de busca
     * @return trecho mais relevante
     */
    String findCloseMatch(String query);

    /**
     * Responde a uma mensagem do usuário usando RAG + histórico de conversa.
     *
     * @param conversationId identificador único da sessão (UUID gerado pelo cliente)
     * @param userMessage    mensagem enviada pelo usuário
     * @return resposta gerada pelo modelo
     */
    String ask(String conversationId, String userMessage);
}
