package br.ufrn.imd.capivaai.domain.model;

public record AskResponse(
        String answer,
        TicketStatus ticketStatus
) {
}
