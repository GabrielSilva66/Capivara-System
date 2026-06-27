package com.example.capivacore.modules.service;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.model.enums.Severity;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.domain.repository.TicketRepository;
import com.example.capivacore.domain.service.TicketService;
import com.example.capivacore.modules.client.AiGraphQlClient;
import com.example.capivacore.modules.client.AiGraphQlClient.AiAskResponse;
import com.example.capivacore.modules.util.TicketIdGenerator;
import com.example.capivacore.modules.util.StatusTicketParser;
import com.example.capivacore.modules.web.dto.EscalatedTicketDTO;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;
import com.example.capivacore.modules.web.dto.SupportResponseDTO;
import com.example.capivacore.modules.web.mapper.TicketMapper;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final TicketRepository   ticketRepository;
    private final AiGraphQlClient   aiGraphQlClient;
    private final TicketIdGenerator ticketIdGenerator;


    @Override
    @Bulkhead(name = "capiva-ai", fallbackMethod = "processSupportFallback")
    @CircuitBreaker(name = "capiva-ai", fallbackMethod = "processSupportFallback")
    @Retry(name = "capiva-ai")
    public SupportResponseDTO processSupport(SupportRequestDTO request) {

        String ticketId = ticketIdGenerator.resolveOrGenerate(request.conversationId());

        log.info("[MS1] processSupport | user={} | ticketId={} | severity={}",
                request.userName(), ticketId, request.severity());

        AiAskResponse aiResponse = aiGraphQlClient.ask(request, ticketId);

        if (aiResponse == null) {
            return processSupportFallback(request, new IllegalStateException("MS2 retornou null"));
        }

        StatusTicket status = StatusTicketParser.parseOrWaiting(aiResponse.ticketStatus());

        if (StatusTicket.RESOLVED.equals(status)) {
            log.info("[MS1] Chamado resolvido pela IA — nenhum ticket criado");
            return new SupportResponseDTO(aiResponse.answer(), ticketId, StatusTicket.RESOLVED);
        }

        Ticket ticket = TicketMapper.toDTO(request, status, ticketId);
        Ticket saved  = ticketRepository.save(ticket);

        log.info("[MS1] Ticket persistido | id={} | status={}", saved.getTicketId(), status);

        return new SupportResponseDTO(aiResponse.answer(), saved.getTicketId(), status);
    }

    /**
     * Fallback acionado quando o Circuit Breaker está aberto ou após esgotamento
     * das retentativas. Persiste um ticket {@code WAITING} sem passar pela IA.
     *
     */
    public SupportResponseDTO processSupportFallback(SupportRequestDTO request,
                                                     Throwable ex) {
        log.error("[MS1] Fallback ativado — MS2 indisponível: {}", ex.getMessage());
        String conversationId = ticketIdGenerator.resolveOrGenerate(request.conversationId());

        Ticket ticket = TicketMapper.toDTO(request, StatusTicket.WAITING, conversationId);
        Ticket saved  = ticketRepository.save(ticket);

        return new SupportResponseDTO(
                "Nosso assistente está temporariamente indisponível. " +
                "Seu chamado foi registrado com o ID " + saved.getTicketId() +
                " e será atendido em breve pela nossa equipe.",
                saved.getTicketId(),
                StatusTicket.WAITING
        );
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public Optional<Ticket> findById(String id) {
        return ticketRepository.findById(id);
    }

    @Override
    public Ticket updateStatus(String id, String newStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado: " + id));
        ticket.setStatus(StatusTicketParser.parse(newStatus, ticket.getStatus()));
        return ticketRepository.save(ticket);
    }

    @Override
    public void updateSeverity() {

    }


    @Transactional
    private void updateLocalTickets(List<EscalatedTicketDTO> escalations) {
        for (EscalatedTicketDTO esc : escalations) {
            ticketRepository.findById(esc.ticketId()).ifPresent(ticket -> {
                ticket.setSeverity(parseSeverity(esc.newSeverity()));
                ticket.setStatus(StatusTicket.ESCALATED_GITHUB);
                ticketRepository.save(ticket);
            });
        }
    }
    private Severity parseSeverity(String raw) {
        try {
            return Severity.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            return Severity.MID;
        }
    }

}
