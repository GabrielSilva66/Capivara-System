package com.example.capivacore.modules.persistence.repository;

import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.modules.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, String> {

    List<TicketEntity> findByStatus(StatusTicket status);
}

