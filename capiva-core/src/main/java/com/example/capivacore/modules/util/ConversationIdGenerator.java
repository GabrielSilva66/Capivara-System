package com.example.capivacore.modules.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Gerador de IDs de conversa para o módulo de suporte.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationIdGenerator {

    public static final String PREFIX = "TICKET_";
    private static final int DIGITS = 6;
    private static final Pattern FORMAT_PATTERN =
            Pattern.compile("^TICKET_\\d{1," + DIGITS + "}$", Pattern.CASE_INSENSITIVE);
    private final JdbcTemplate jdbcTemplate;


    public String generate() {
        Long next = jdbcTemplate.queryForObject(
                "SELECT nextval('ticket_conversation_seq')", Long.class);
        String id = PREFIX + String.format("%0" + DIGITS + "d", next);
        log.debug("[ConversationIdGenerator] Gerado: {}", id);
        return id;
    }

    public String resolveOrGenerate(String incoming) {
        if (incoming == null || incoming.isBlank()) {
            return generate();
        }
        if (!FORMAT_PATTERN.matcher(incoming.trim()).matches()) {
            log.warn("[ConversationIdGenerator] Formato inválido '{}' — gerando novo ID", incoming);
            return generate();
        }
        // Normaliza para maiúsculas (ex.: "ticket_000001" → "TICKET_000001")
        return incoming.trim().toUpperCase();
    }
}
