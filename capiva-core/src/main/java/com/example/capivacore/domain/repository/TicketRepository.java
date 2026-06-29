package com.example.capivacore.domain.repository;

import com.example.capivacore.domain.model.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída (driven port) para persistência de tickets.
 * Implementada pelo adapter {@code TicketRepositoryImpl} na camada de persistência.
 */
public interface TicketRepository {

    /** Persiste um ticket (insert ou update). */
    Ticket save(Ticket ticket);

    /** Lista todos os tickets. */
    Page<Ticket> findAll(Pageable pageable);

    /** Busca um ticket pelo seu UUID. */
    Optional<Ticket> findById(String id);

    /** Lista tickets por status. */
    List<Ticket> findByStatus(String status);

    void delete(String ticketId);
}

