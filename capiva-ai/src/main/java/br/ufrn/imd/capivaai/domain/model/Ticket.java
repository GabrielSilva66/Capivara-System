package br.ufrn.imd.capivaai.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Ticket(
        String id,
        String userName,
        String title,
        String description,
        String severity,
        String status,
        String createdAt
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static Ticket createNew(int nextId, String userName, String title, String description, String severity) {
        return new Ticket(
                "TK-%04d".formatted(nextId),
                userName,
                title,
                description,
                severity,
                "AGUARDANDO",
                LocalDateTime.now().format(FMT)
        );
    }

    public static Ticket preLoaded(int id, String title, String description, String severity) {
        return new Ticket(
                "TK-%04d".formatted(id),
                "Sistema",
                title,
                description,
                severity,
                "AGUARDANDO",
                LocalDateTime.now().minusHours(id % 4 + 1).format(FMT)
        );
    }
}
