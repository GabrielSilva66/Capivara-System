package com.example.capivacore.modules.web.mapper;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;

import static com.example.capivacore.modules.util.StatusTicketParser.parseSeverity;

public class TicketMapper {

    public static Ticket toDTO(SupportRequestDTO request, StatusTicket status, String conversationId){
        Ticket ticket = new Ticket();
        ticket.setTicketId(conversationId);
        ticket.setUserName(request.userName());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setSeverity(parseSeverity(request.severity()));
        ticket.setStatus(status);
        return ticket;
    }


//    public static toEntity(){
//
//    }
}
