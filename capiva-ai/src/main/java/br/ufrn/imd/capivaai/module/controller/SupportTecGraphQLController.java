package br.ufrn.imd.capivaai.module.controller;

import br.ufrn.imd.capivaai.domain.service.SupportTecService;
import br.ufrn.imd.capivaai.module.dto.request.AskInputDTO;
import br.ufrn.imd.capivaai.module.dto.response.ChatResponseDTO;
import br.ufrn.imd.capivaai.module.util.DocumentReader;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Resolver GraphQL para o módulo de Suporte Técnico com IA.
 *
 * <p>Operações disponíveis:
 * <ul>
 *   <li><b>Query</b> {@code findKnowledgeMatches} – busca semântica na VectorStore.</li>
 *   <li><b>Mutation</b> {@code ask} – chat com RAG + memória de conversa.</li>
 *   <li><b>Mutation</b> {@code ingestKnowledge} – indexa textos livres na VectorStore.</li>
 *   <li><b>Mutation</b> {@code ingestDocument} – lê um arquivo via TikaDocumentReader
 *       e indexa seu conteúdo na VectorStore.</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class SupportTecGraphQLController {

    private final SupportTecService supportTecService;

    @QueryMapping
    public List<String> findKnowledgeMatches(
            @Argument String query,
            @Argument int topK) {
        return supportTecService.findClosestMatches(query, topK);
    }

    @MutationMapping
    public Flux<String> ask(@Argument AskInputDTO input) {
        return supportTecService.ask(input.getConversationId(), input.getMessage());
//        return ChatResponseDTO.builder()
//                .conversationId(input.getConversationId())
//                .answer(answer)
//                .build();
    }

    @MutationMapping
    public boolean ingestKnowledge(@Argument List<String> texts) {
        supportTecService.add(texts);
        return true;
    }

    @MutationMapping
    public boolean ingestDocument(@Argument String fileUrl) {
        supportTecService.ingestDocument(fileUrl);
        return true;
    }
}
