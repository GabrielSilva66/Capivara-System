package com.example.capivacore.modules.service;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.domain.repository.TicketRepository;
import com.example.capivacore.domain.service.TicketService;
import com.example.capivacore.modules.client.AiGraphQlClient;
import com.example.capivacore.modules.client.AiGraphQlClient.AiAskResponse;
import com.example.capivacore.modules.client.ScalatorClient;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import static com.example.capivacore.modules.util.StatusTicketParser.parseSeverity;

/**
 * Implementação do orquestrador de suporte do MS1.
 *
 * <p>Fluxo principal:
 * <ol>
 *   <li>Persiste o ticket imediatamente com status {@code WAITING} —
 *       o banco gera o {@code ticket_id} via {@code generate_ticket_id()}.</li>
 *   <li>Usa o {@code ticket_id} gerado como referência ao chamar a IA (MS2),
 *       permitindo que ele seja rastreado na issue do GitHub e pelo serverless
 *       de escalonamento (capiva-scalator).</li>
 *   <li>Atualiza o status do ticket com base na resposta da IA.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TickerServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final AiGraphQlClient  aiGraphQlClient;
    private final ScalatorClient   scalatorClient;

    @Override
    @Transactional
    @Bulkhead(name = "capiva-ai", fallbackMethod = "processSupportFallback")
    @CircuitBreaker(name = "capiva-ai", fallbackMethod = "processSupportFallback")
    public SupportResponseDTO processSupport(SupportRequestDTO request) {

        Ticket pending = TicketMapper.toTicket(request, StatusTicket.WAITING);
        Ticket saved   = ticketRepository.save(pending);
        String ticketId = saved.getTicketId();

        log.info("[MS1] Ticket criado | id={} | user={} | severity={}",
                ticketId, request.userName(), request.severity());

        AiAskResponse aiResponse = aiGraphQlClient.ask(request, ticketId);

        if (aiResponse == null) {
            log.warn("[MS1] MS2 retornou null — ticket {} permanece em WAITING", ticketId);
            return new SupportResponseDTO(
                    "Nosso assistente está processando seu chamado. Ticket: " + ticketId,
                    ticketId,
                    StatusTicket.WAITING
            );
        } else if (aiResponse.answer().isBlank()) {
            log.warn("[MS1] MS2 retornou resposta vazia — ticket {} em WAITING", ticketId);
            return new SupportResponseDTO(
                    "Nosso assistente está processando seu chamado. Ticket: " + ticketId,
                    ticketId,
                    StatusTicket.WAITING
            );
        }

        StatusTicket finalStatus = StatusTicketParser.parseOrWaiting(aiResponse.ticketStatus());
        if(finalStatus == StatusTicket.RESOLVED){
            ticketRepository.delete(saved.getTicketId());
        }else{
            saved.setStatus(finalStatus);
            ticketRepository.save(saved);
        }

        log.info("[MS1] Ticket | id={} | status={}", ticketId, finalStatus);

        return new SupportResponseDTO(aiResponse.answer(), ticketId, finalStatus);
    }

    /**
     * Fallback acionado quando o Circuit Breaker está aberto ou após esgotamento
     * das retentativas. O ticket já foi persistido com status {@code WAITING};
     * apenas informa o usuário que a IA está indisponível.
     */
    public SupportResponseDTO processSupportFallback(SupportRequestDTO request, Throwable ex) {
        log.error("[MS1] Fallback ativado — MS2 indisponível: {}", ex.getMessage());

        // Ticket salvo antes da chamada à IA — persiste novamente em caso de falha total
        Ticket pending = TicketMapper.toTicket(request, StatusTicket.WAITING);
        Ticket saved   = ticketRepository.save(pending);

        return new SupportResponseDTO(
                "Nosso assistente está temporariamente indisponível. " +
                "Seu chamado foi registrado com o ID " + saved.getTicketId() +
                " e será atendido em breve pela nossa equipe.",
                saved.getTicketId(),
                StatusTicket.WAITING
        );
    }

    @Override
    public Page<Ticket> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable);
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
    @Retry(name = "escalator")
    public void runSlaEscalation() {
        List<EscalatedTicketDTO> escalations = scalatorClient.triggerEscalation();
        for (var esc : escalations) {
            ticketRepository.findById(esc.ticketId()).ifPresent(ticket -> {
                ticket.setSeverity(parseSeverity(esc.newSeverity()));
                ticket.setStatus(StatusTicket.ESCALATED_GITHUB);
                ticketRepository.save(ticket);
            });
        }
    }
}
