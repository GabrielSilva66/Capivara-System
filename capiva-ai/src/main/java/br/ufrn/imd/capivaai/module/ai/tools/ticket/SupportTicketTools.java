package br.ufrn.imd.capivaai.module.ai.tools.ticket;

import br.ufrn.imd.capivaai.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SupportTicketTools {

    private final TicketQueueService queueService;

    @Tool(description = """
            Cria um ticket de suporte para atendimento humano.
            Chame listTickets() ANTES para verificar duplicatas.
            Retorna o número do ticket e a posição do usuário na fila.
            """)
    public String createTicket(
            @ToolParam(description = "Nome do usuário") String userName,
            @ToolParam(description = "Título curto do problema") String title,
            @ToolParam(description = "Descrição detalhada do problema") String description,
            @ToolParam(description = "Severidade: BAIXA, MEDIA, ALTA ou CRITICA") String severity) {

        Ticket ticket = queueService.create(userName, title, description, severity);
        int position = queueService.getPosition(ticket.id());

        return """
                Ticket criado com sucesso!
                ID: %s
                Título: %s
                Severidade: %s
                Status: %s
                Posição na fila: %d° lugar
                Criado em: %s
                """.formatted(ticket.id(), ticket.title(), ticket.severity(),
                ticket.status(), position, ticket.createdAt());
    }
}
