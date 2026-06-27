package com.example.capivacore.modules.util;

import com.example.capivacore.domain.model.enums.Severity;
import com.example.capivacore.domain.model.enums.StatusTicket;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitário centralizado para parse e normalização de StatusTicket.
 */
@Slf4j
public final class StatusTicketParser {

    private StatusTicketParser() {}

    public static StatusTicket parse(String raw, StatusTicket fallback) {
        if (raw == null || raw.isBlank()) {
            log.warn("[StatusTicketParser] Status nulo/vazio — usando fallback: {}", fallback);
            return fallback;
        }
        try {
            return StatusTicket.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[StatusTicketParser] Status desconhecido '{}' — usando fallback: {}", raw, fallback);
            return fallback;
        }
    }

    public static StatusTicket parseOrWaiting(String raw) {
        return parse(raw, StatusTicket.WAITING);
    }

    public static Severity parseSeverity(String rawSeverity) {
        return switch (rawSeverity.toUpperCase()) {
            case "BAIXA"   -> Severity.LOW;
            case "ALTA"    -> Severity.HIGH;
            case "CRITICA" -> Severity.URGENT;
            default        -> Severity.MID;
        };
    }
}
