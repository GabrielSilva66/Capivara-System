package com.example.capivacore.domain.service;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;
import com.example.capivacore.modules.web.dto.SupportResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordena o fluxo: recebe os dados do formulário via REST, chama o MS2 via
 * GraphQL, interpreta o {@code ticketStatus} retornado pela IA e decide se
 * persiste o ticket no banco ou não.
 */
public interface TicketService {

    SupportResponseDTO processSupport(SupportRequestDTO request);

    List<Ticket> findAll();

    Optional<Ticket> findById(UUID id);

    Ticket updateStatus(UUID id, String newStatus);
}

