package com.example.capivacore.modules.persistence.adapter;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.model.enums.StatusTicket;
import com.example.capivacore.domain.repository.TicketRepository;
import com.example.capivacore.modules.persistence.entity.TicketEntity;
import com.example.capivacore.modules.persistence.mapper.TicketMapper;
import com.example.capivacore.modules.persistence.repository.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepository {

    private final TicketJpaRepository jpaRepository;

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = TicketMapper.toEntity(ticket);
        TicketEntity saved  = jpaRepository.save(entity);
        return TicketMapper.toModel(saved);
    }

    @Override
    public Page<Ticket> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(TicketMapper::toModel);
    }

    @Override
    public Optional<Ticket> findById(String id) {
        return jpaRepository.findById(id).map(TicketMapper::toModel);
    }

    @Override
    public List<Ticket> findByStatus(String status) {
        StatusTicket st = StatusTicket.valueOf(status.toUpperCase());
        return jpaRepository.findByStatus(st)
                .stream()
                .map(TicketMapper::toModel)
                .toList();
    }

    @Override
    public void delete(String ticketId){
        jpaRepository.deleteById(ticketId);
    }
}

