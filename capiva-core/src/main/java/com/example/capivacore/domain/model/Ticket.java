package com.example.capivacore.domain.model;

import com.example.capivacore.domain.model.enums.Severity;
import com.example.capivacore.domain.model.enums.StatusTicket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private UUID id;
    private String conversationId;
    private String userName;
    private String title;
    private String description;
    private Severity severity;
    private StatusTicket status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
