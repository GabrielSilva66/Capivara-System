package com.example.capivacore.domain.model.enums;

/**
 * Status do ticket persistido no MS1.
 * <p>
 * Os valores {@code RESOLVED}, {@code WAITING} e {@code ESCALATED_GITHUB}
 * espelham exatamente o enum {@code TicketStatus} do MS2, permitindo mapeamento
 * direto sem conversão extra no serviço.
 */
public enum StatusTicket {
    /** Problema resolvido pela IA — ticket NÃO é persistido no banco. */
    RESOLVED,
    /** Ticket criado e na fila interna; aguarda atendimento humano. */
    WAITING,
    /** Ticket criado + issue registrada no GitHub pela IA. */
    ESCALATED_GITHUB,
    // ── legado / uso interno ───────────────────────────────────────
    ACTIVE,
    CANCELED,
    FINISHED
}
