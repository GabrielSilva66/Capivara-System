package com.example.capivacore.modules.persistence.mapper;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.modules.persistence.entity.TicketEntity;

public class TicketMapper {

    private TicketMapper() {}

    public static TicketEntity toEntity(Ticket ticket) {
        TicketEntity entity = new TicketEntity();
        // ticket_id é gerado automaticamente pelo PostgreSQL via generate_ticket_id()
        entity.setUserName(ticket.getUserName());
        entity.setTitle(ticket.getTitle());
        entity.setDescription(ticket.getDescription());
        entity.setSeverity(ticket.getSeverity());
        entity.setStatus(ticket.getStatus());
        return entity;
    }

    public static Ticket toModel(TicketEntity entity) {
        return new Ticket(
                entity.getTicketId(),
                entity.getUserName(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

