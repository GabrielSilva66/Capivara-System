package br.ufrn.imd.capivaai.module.ai.tools.ticket;

import br.ufrn.imd.capivaai.domain.model.Ticket;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class TicketQueueService {

    private final TicketQueueProperties properties;
    private final CopyOnWriteArrayList<Ticket> queue = new CopyOnWriteArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(1000);

    @PostConstruct
    public void init() {
        int startId = 1001;
        for (TicketQueueProperties.TicketEntry entry : properties.getInitialTickets()) {
            queue.add(Ticket.preLoaded(startId, entry.getTitle(), entry.getDescription(), entry.getSeverity()));
            startId++;
        }
        idCounter.set(startId);
    }

    public List<Ticket> listAll() {
        return Collections.unmodifiableList(queue);
    }

    public Ticket create(String userName, String title, String description, String severity) {
        Ticket ticket = Ticket.createNew(idCounter.incrementAndGet(), userName, title, description, severity);
        queue.add(ticket);
        return ticket;
    }

    public int getPosition(String ticketId) {
        List<Ticket> waiting = queue.stream()
                .filter(t -> "AGUARDANDO".equals(t.status()))
                .toList();
        for (int i = 0; i < waiting.size(); i++) {
            if (waiting.get(i).id().equals(ticketId)) return i + 1;
        }
        return -1;
    }
}
