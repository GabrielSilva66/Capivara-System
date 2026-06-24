package com.example.capivacore.modules.client;

import com.example.capivacore.modules.web.dto.SupportRequestDTO;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;


@Slf4j
@Component
public class AiGraphQlClient {

    private static final String ASK_MUTATION = """
            mutation Ask($input: AskInput!) {
              ask(input: $input) {
                answer
                ticketStatus
              }
            }
            """;

    private final HttpGraphQlClient graphQlClient;

    public AiGraphQlClient(WebClient.Builder builder, @Value("${capiva_ai_url}") String graphQlUrl) {
        this.graphQlClient = HttpGraphQlClient
                .builder(
                        builder.baseUrl(graphQlUrl).build()
                ).build();
    }


    @RateLimiter(name = "capiva-ai-client")
    public AiAskResponse ask(SupportRequestDTO request, String conversationId) {
        log.info("[AiGraphQlClient] Chamando MS2 | conversationId={} | status=?",
                conversationId);

        AiAskResponse response = graphQlClient.document(ASK_MUTATION)
                .variable("input", Map.of(
                        "conversationId", conversationId,
                        "userName",       request.userName(),
                        "title",          request.title(),
                        "description",    request.description(),
                        "severity",       request.severity()
                ))
                .retrieve("ask")
                .toEntity(AiAskResponse.class)
                .block();

        log.info("[AiGraphQlClient] MS2 respondeu | ticketStatus={}", 
                response != null ? response.ticketStatus() : "null");

        return response;
    }

    public record AiAskResponse(String answer, String ticketStatus) {}
}
