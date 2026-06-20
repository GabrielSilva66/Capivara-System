package br.ufrn.imd.capivaai.module.ai.tools.ticket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.ticket.queue")
public class TicketQueueProperties {

    private List<TicketEntry> initialTickets = new ArrayList<>();

    @Data
    public static class TicketEntry {
        private String title;
        private String description;
        private String severity;
    }
}
