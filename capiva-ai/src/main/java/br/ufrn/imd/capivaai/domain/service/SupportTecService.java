package br.ufrn.imd.capivaai.domain.service;

import br.ufrn.imd.capivaai.domain.model.AskResponse;
import br.ufrn.imd.capivaai.module.dto.request.AskInputDTO;

import java.util.List;

/**
 * Porta de domínio do módulo de Suporte Técnico com IA.
 * <p>
 * Casos de uso:
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
     * Parsing e ingestão de documento a partir de URL.
     *
     * @param fileUrl URL do arquivo a ser indexado
     */
    void ingestDocument(String fileUrl);

    /**
     * Recupera os {@code numberOfMatches} trechos mais similares à {@code query}.
     *
     * @param query           texto de busca
     * @param numberOfMatches quantidade de resultados
     * @return lista de trechos similares
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
     * Processa a mensagem do usuário com RAG + memória + ferramentas MCP.
     * <p>
     * Injeta os dados estruturados do chamado ({@code userName}, {@code title},
     * {@code description}, {@code severity}) como contexto no prompt da IA,
     * permitindo que ela crie o ticket e a issue no GitHub sem precisar
     * perguntar ao usuário durante o chat.
     *
     * @param input DTO com todos os dados do chamado e a mensagem do usuário
     * @return resposta estruturada com o texto para o usuário e o status do ticket
     */
    AskResponse ask(AskInputDTO input);
}

