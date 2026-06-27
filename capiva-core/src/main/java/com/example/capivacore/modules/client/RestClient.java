package com.example.capivacore.modules.client;

import com.example.capivacore.modules.web.dto.EscalatedTicketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Slf4j
@Component
@RequiredArgsConstructor
public class RestClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${capiva.scalator.url}")
    private String scalatorUrl;

    public void checkSlaEscalations() {
        log.info("[SLA Scheduler] Iniciando verificação de SLA junto à Serverless Function...");
        try {
            List<EscalatedTicketDTO> response = Arrays
                    .stream(Objects.requireNonNull(restTemplate.postForObject(scalatorUrl, null, EscalatedTicketDTO[].class)))
                    .toList();

            if (!response.isEmpty()) {
                log.info("[SLA Scheduler] {} tickets foram escalados. Atualizando banco local...", response.size());
            }
        } catch (Exception e) {
            log.error("[SLA Scheduler] Falha ao consultar Serverless Function de SLA: {}", e.getMessage());
        }
    }

}
