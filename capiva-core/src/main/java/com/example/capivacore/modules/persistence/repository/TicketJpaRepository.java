package com.example.capivacore.modules.persistence.repository;

import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.modules.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findByStatus(StatusTicket status);
}

