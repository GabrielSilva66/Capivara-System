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
@RestController("/api")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @PostMapping("/support/ask")
    public ResponseEntity<SupportResponseDTO> ask(@Valid @RequestBody SupportRequestDTO request) {

        SupportResponseDTO response = service.processSupport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable UUID id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        Ticket updated = service.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }


    @PostMapping("update_severity")
    public ResponseEntity<Void> updateSeverity(){

        return (ResponseEntity<Void>) ResponseEntity.ok();
    }
}

