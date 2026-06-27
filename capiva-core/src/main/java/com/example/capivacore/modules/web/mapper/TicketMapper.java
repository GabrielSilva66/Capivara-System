package com.example.capivacore.modules.web.mapper;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;

import static com.example.capivacore.modules.util.StatusTicketParser.parseSeverity;

public class TicketMapper {

    private TicketMapper() {}

    /**
     * Cria um {@link Ticket} de domínio a partir do request do front.
     * O {@code ticketId} é intencionalmente deixado nulo — o banco o gera
     * automaticamente via {@code generate_ticket_id()} no INSERT.
     */
    public static Ticket toTicket(SupportRequestDTO request, StatusTicket status) {
        Ticket ticket = new Ticket();
        // ticketId = null → será preenchido pelo DEFAULT do PostgreSQL
        ticket.setUserName(request.userName());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setSeverity(parseSeverity(request.severity()));
        ticket.setStatus(status);
        return ticket;
    }
}
