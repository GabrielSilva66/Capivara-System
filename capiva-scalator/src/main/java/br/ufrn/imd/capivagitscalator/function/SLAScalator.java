package br.ufrn.imd.capivagitscalator.function;

import br.ufrn.imd.capivagitscalator.domain.Severity;
import br.ufrn.imd.capivagitscalator.domain.StatusTicket;
import br.ufrn.imd.capivagitscalator.dto.EscalatedTicketDTO;
import br.ufrn.imd.capivagitscalator.util.SeverityUtil;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class SLAScalator {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.repository}")
    private String githubRepoName;

    private static final long TIME_TO_LOAD_MS = 2 * 60 * 1000;

    @Bean
    public Supplier<List<EscalatedTicketDTO>> escalateStaleTickets() {
        return () -> {
            List<EscalatedTicketDTO> escalatedTickets = new ArrayList<>();
            System.out.println("[Serverless - SLA] Iniciando varredura de issues estagnadas...");

            try {
                GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
                GHRepository repository = github.getRepository(githubRepoName);

                List<GHIssue> openIssues = repository.getIssues(GHIssueState.OPEN);

                Date limiteSla = new Date(System.currentTimeMillis() - TIME_TO_LOAD_MS);

                for (GHIssue issue : openIssues) {
                    if (issue.isPullRequest()) continue;
                    if (issue.getCreatedAt().before(limiteSla)) {

                        Severity severity = SeverityUtil.find(issue.getLabels());

                        boolean isFinished = issue.getLabels().stream()
                                .anyMatch(label -> label.getName().equalsIgnoreCase(StatusTicket.FINISHED.toString()));

                        if (severity != Severity.URGENT && !isFinished) {
                            log.info("[SLA ESTOURADO] Escalonando Issue #{}", issue.getNumber());

                            String upSeverity = SeverityUtil.upSeverity(severity).toString();
                            issue.addLabels(upSeverity);
                            issue.comment("️ **[SLA ESCALATOR]** Este ticket estourou o limite de tempo de resposta sem atendimento e foi escalado automaticamente para **CRÍTICO**.");

                            String ticketId = extractTicketId(issue.getBody());
                            if(ticketId != null){
                                escalatedTickets.add(new EscalatedTicketDTO(upSeverity, ticketId));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[ERRO] Falha ao comunicar com a API do GitHub: " + e.getMessage());
            }
            return escalatedTickets;
        };
    }

    private String extractTicketId(String body) {
        return null;
    }
}
