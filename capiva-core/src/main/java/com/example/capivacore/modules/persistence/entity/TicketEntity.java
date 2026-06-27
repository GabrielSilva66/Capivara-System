package com.example.capivacore.modules.persistence.entity;

import com.example.capivacore.domain.model.enums.Severity;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "ticketId")
@Entity
@Table(name = "CP_TICKET")
public class TicketEntity {

    @Id
    @Generated(event = EventType.INSERT)
    @Column(name = "ticket_id",
            nullable = false,
            length = 50,
            insertable = false,
            updatable = false,
            columnDefinition = "VARCHAR(50) DEFAULT generate_ticket_id()")
    private String ticketId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity = Severity.MID;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_ticket", nullable = false)
    private StatusTicket status = StatusTicket.WAITING;

    @Column(name = "dt_created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "dt_updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

