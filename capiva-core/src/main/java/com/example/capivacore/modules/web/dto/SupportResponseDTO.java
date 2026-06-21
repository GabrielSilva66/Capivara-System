package com.example.capivacore.modules.web.dto;

import com.example.capivacore.domain.model.enums.StatusTicket;

import java.util.UUID;

/**
 * DTO de resposta retornado ao cliente após o processamento do chamado.
 *
 * @param answer         resposta textual da IA para exibição ao usuário
 * @param conversationId ID da conversa gerado pelo MS1 — deve ser reutilizado pelo
 *                       cliente em mensagens subsequentes para manter o contexto
 * @param ticketId       UUID do ticket persistido no MS1 ({@code null} se {@code RESOLVED})
 * @param ticketStatus   status do chamado definido pela IA
 */
public record SupportResponseDTO(
        String answer,
        String conversationId,
        UUID ticketId,
        StatusTicket ticketStatus
) {}
