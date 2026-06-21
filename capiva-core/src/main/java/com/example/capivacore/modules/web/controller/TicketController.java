package com.example.capivacore.modules.web.controller;

import com.example.capivacore.domain.model.Ticket;
import com.example.capivacore.domain.service.TicketService;
import com.example.capivacore.modules.web.dto.SupportRequestDTO;
import com.example.capivacore.modules.web.dto.SupportResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    /**
     * Ponto de entrada principal: recebe os dados do formulário + mensagem do usuário,
     * delega ao serviço que chama o MS2 via GraphQL e persiste o ticket se necessário.
     */
    @PostMapping("/api/support/ask")
    public ResponseEntity<SupportResponseDTO> ask(@Valid @RequestBody SupportRequestDTO request) {

//        log.info("[TicketController] POST /api/support/ask | user={}", request.userName());

        SupportResponseDTO response = service.processSupport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/tickets")
    public ResponseEntity<List<Ticket>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/api/tickets/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable UUID id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/api/tickets/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        Ticket updated = service.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}

