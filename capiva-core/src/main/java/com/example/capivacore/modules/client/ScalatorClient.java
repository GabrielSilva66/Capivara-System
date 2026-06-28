package com.example.capivacore.modules.client;

import com.example.capivacore.modules.web.dto.EscalatedTicketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class ScalatorClient {

    private final WebClient webClient;

    public ScalatorClient(WebClient.Builder webClientBuilder, @Value("${capiva.scalator.url}") String scalatorUrl) {
        this.webClient = webClientBuilder.baseUrl(scalatorUrl).build();
    }
    public List<EscalatedTicketDTO> triggerEscalation() {
        try {
            return webClient.get()
                    .uri("/escalateStaleTickets")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<EscalatedTicketDTO>>() {})
                    .block();
        } catch (Exception e) {
            log.error("[ScalatorClient] Erro na comunicação com a Serverless Function: {}", e.getMessage());
            return List.of();
        }
    }

}
