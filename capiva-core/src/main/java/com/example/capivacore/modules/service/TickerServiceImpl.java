package com.example.capivacore.modules.service;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.model.enums.Severity;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.domain.repository.TicketRepository;
import com.example.capivacore.domain.service.TicketService;
import com.example.capivacore.modules.client.AiGraphQlClient;
import com.example.capivacore.modules.client.AiGraphQlClient.AiAskResponse;
import com.example.capivacore.modules.util.ConversationIdGenerator;
import com.example.capivacore.modules.util.StatusTicketParser;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;
import com.example.capivacore.modules.web.dto.SupportResponseDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do orquestrador de suporte do MS1.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TickerServiceImpl implements TicketService {

    private final TicketRepository       ticketRepository;
    private final AiGraphQlClient        aiGraphQlClient;
    private final ConversationIdGenerator conversationIdGenerator;


    @Override
    @Bulkhead(name = "capiva-ai", fallbackMethod = "processSupportFallback")
    @CircuitBreaker(name = "capiva-ai", fallbackMethod = "processSupportFallback")
    @Retry(name = "capiva-ai")
    public SupportResponseDTO processSupport(SupportRequestDTO request) {

        String conversationId = conversationIdGenerator.resolveOrGenerate(request.conversationId());

        log.info("[MS1] processSupport | user={} | conversationId={} | severity={}",
                request.userName(), conversationId, request.severity());

        AiAskResponse aiResponse = aiGraphQlClient.ask(request, conversationId);

        if (aiResponse == null) {
            return processSupportFallback(request, new IllegalStateException("MS2 retornou null"));
        }

        StatusTicket status = StatusTicketParser.parseOrWaiting(aiResponse.ticketStatus());

        if (StatusTicket.RESOLVED.equals(status)) {
            log.info("[MS1] Chamado resolvido pela IA — nenhum ticket criado");
            return new SupportResponseDTO(aiResponse.answer(), conversationId, null, StatusTicket.RESOLVED);
        }

        Ticket ticket = buildTicket(request, status, conversationId);
        Ticket saved  = ticketRepository.save(ticket);

        log.info("[MS1] Ticket persistido | id={} | status={}", saved.getId(), status);

        return new SupportResponseDTO(aiResponse.answer(), conversationId, saved.getId(), status);
    }

    /**
     * Fallback acionado quando o Circuit Breaker está aberto ou após esgotamento
     * das retentativas. Persiste um ticket {@code WAITING} sem passar pela IA.
     *
     */
    public SupportResponseDTO processSupportFallback(SupportRequestDTO request,
                                                     Throwable ex) {
        log.error("[MS1] Fallback ativado — MS2 indisponível: {}", ex.getMessage());
        String conversationId = conversationIdGenerator.resolveOrGenerate(request.conversationId());

        Ticket ticket = buildTicket(request, StatusTicket.WAITING, conversationId);
        Ticket saved  = ticketRepository.save(ticket);

        return new SupportResponseDTO(
                "Nosso assistente está temporariamente indisponível. " +
                "Seu chamado foi registrado com o ID " + saved.getId() +
                " e será atendido em breve pela nossa equipe.",
                conversationId,
                saved.getId(),
                StatusTicket.WAITING
        );
    }


    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id);
    }

    @Override
    public Ticket updateStatus(UUID id, String newStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado: " + id));
        // Usa o parser para garantir normalização uppercase + inglês
        ticket.setStatus(StatusTicketParser.parse(newStatus, ticket.getStatus()));
        return ticketRepository.save(ticket);
    }

    // Helpers privados

    private Ticket buildTicket(SupportRequestDTO request, StatusTicket status, String conversationId) {
        Ticket ticket = new Ticket();
        ticket.setConversationId(conversationId);
        ticket.setUserName(request.userName());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setSeverity(parseSeverity(request.severity()));
        ticket.setStatus(status);
        return ticket;
    }

    private Severity parseSeverity(String rawSeverity) {
        return switch (rawSeverity.toUpperCase()) {
            case "BAIXA"   -> Severity.LOW;
            case "MEDIA"   -> Severity.MID;
            case "ALTA"    -> Severity.HIGH;
            case "CRITICA" -> Severity.URGENT;
            default        -> Severity.MID;
        };
    }
}
