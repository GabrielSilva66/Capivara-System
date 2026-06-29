package com.example.capivacore.domain.service;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;
import com.example.capivacore.modules.web.dto.SupportResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Coordena o fluxo: recebe os dados do formulário via REST, chama o MS2 via
 * GraphQL, interpreta o {@code ticketStatus} retornado pela IA e decide se
 * persiste o ticket no banco ou não.
 */
public interface TicketService {

    SupportResponseDTO processSupport(SupportRequestDTO request);

    Page<Ticket> findAll(Pageable pageable);

    Optional<Ticket> findById(String id);

    Ticket updateStatus(String id, String newStatus);

    void runSlaEscalation();
}

