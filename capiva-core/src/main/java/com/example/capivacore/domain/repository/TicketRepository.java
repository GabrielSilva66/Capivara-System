package com.example.capivacore.domain.repository;

import com.example.capivacore.domain.model.Ticket;

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
    List<Ticket> findAll();

    /** Busca um ticket pelo seu UUID. */
    Optional<Ticket> findById(UUID id);

    /** Lista tickets por status. */
    List<Ticket> findByStatus(String status);
}

