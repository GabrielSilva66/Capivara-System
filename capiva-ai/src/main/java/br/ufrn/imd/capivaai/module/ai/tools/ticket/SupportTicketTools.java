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
            Lista todos os tickets abertos na fila de atendimento humano.
            SEMPRE chame antes de criar um novo ticket para:
            - verificar se já existe problema similar (evitar duplicata)
            - informar ao usuário quantas pessoas estão na frente dele
            """)
    public String listTickets() {
        List<Ticket> tickets = queueService.listAll();
        if (tickets.isEmpty()) {
            return "Fila vazia. Nenhum ticket aberto no momento.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total na fila: %d ticket(s)\n\n".formatted(tickets.size()));

        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            sb.append("[%d] %s | Severidade: %s\n    %s\n    Aberto em: %s\n\n"
                    .formatted(i + 1, t.title(), t.severity(), t.description(), t.createdAt()));
        }
        return sb.toString();
    }

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
