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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @PostMapping("/support/ask")
    public ResponseEntity<SupportResponseDTO> ask(@Valid @RequestBody SupportRequestDTO request) {

        SupportResponseDTO response = service.processSupport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets")
    public ResponseEntity<Page<Ticket>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        int pageSize = (size == null) ? 10 : size;
        return ResponseEntity.ok(service.findAll(PageRequest.of(page, pageSize)));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable String id, @RequestParam String status) {
        Ticket updated = service.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("update-severity")
    public ResponseEntity<Map<String, String>> updateSeverity(){
        service.runSlaEscalation();
        return ResponseEntity.ok(Map.of("status", "Severidade atualizada!"));
    }
}

