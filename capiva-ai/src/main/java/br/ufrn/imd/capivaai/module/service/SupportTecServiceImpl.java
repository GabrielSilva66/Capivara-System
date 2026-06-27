package br.ufrn.imd.capivaai.module.service;

import br.ufrn.imd.capivaai.domain.model.AskResponse;
import br.ufrn.imd.capivaai.domain.model.TicketStatus;
import br.ufrn.imd.capivaai.domain.repository.SupportTecRepository;
import br.ufrn.imd.capivaai.domain.service.SupportTecService;
import br.ufrn.imd.capivaai.module.dto.request.AskInputDTO;
import br.ufrn.imd.capivaai.module.util.DocumentReader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SupportTecServiceImpl implements SupportTecService {

    /** Regex que captura a tag de status embutida pela IA ao final da resposta. */
    private static final Pattern STATUS_PATTERN =
            Pattern.compile("\\[STATUS:(RESOLVED|WAITING|ESCALATED_GITHUB)\\]");

    private final SupportTecRepository repository;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;
    private final DocumentReader documentReader;

    public SupportTecServiceImpl(
            SupportTecRepository repository,
            @Qualifier("supportTecChatClient") ChatClient chatClient,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            DocumentReader documentReader) {
        this.repository     = repository;
        this.chatClient     = chatClient;
        this.chatMemory     = chatMemory;
        this.vectorStore    = vectorStore;
        this.documentReader = documentReader;
    }

    @Override
    public void add(List<String> information) {
        repository.add(information);
    }

    @Override
    public void ingestDocument(String fileUrl) {
        List<String> texts = documentReader.loadText(fileUrl)
                .stream()
                .map(Document::getText)
                .toList();
        add(texts);
    }

    @Override
    public List<String> findClosestMatches(String query, int numberOfMatches) {
        return repository.findClosestMatches(query, numberOfMatches);
    }

    @Override
    public String findCloseMatch(String query) {
        return repository.findCloseMatch(query);
    }


    @Override
    public AskResponse ask(AskInputDTO input) {

        String enrichedMessage = buildEnrichedMessage(input);
        String rawAnswer = chatClient.prompt()
                .advisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                // ticketId é o ID gerado pelo banco — serve como chave de memória de diálogo
                // e como referência que a IA deve usar ao criar a issue no GitHub
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, input.getTicketId()))
                .user(enrichedMessage)
                .stream()
                .content()
                .collectList()
                .map(chunks -> String.join("", chunks))
                .block();

        if (rawAnswer == null) rawAnswer = "";

        rawAnswer = rawAnswer.replace("\\n", "\n").replace("\\r", "\r");

        TicketStatus ticketStatus = extractStatus(rawAnswer);

        String cleanAnswer = STATUS_PATTERN.matcher(rawAnswer).replaceAll("").strip();

        System.out.printf("[capiva-ai] ticketId=%s | status=%s | answer=%s%n",
                input.getTicketId(), ticketStatus, cleanAnswer);

        return new AskResponse(cleanAnswer, ticketStatus);
    }

    private String buildEnrichedMessage(AskInputDTO input) {
        return """
                [CONTEXTO DO CHAMADO]
                ID do Ticket: %s
                Usuário: %s
                Título: %s
                Descrição: %s
                Severidade: %s

                Use o ID do Ticket acima como referência ao criar a issue no GitHub.
                """.formatted(
                input.getTicketId(),
                input.getUserName(),
                input.getTitle(),
                input.getDescription(),
                input.getSeverity()
        );
    }

    private TicketStatus extractStatus(String rawAnswer) {
        Matcher m = STATUS_PATTERN.matcher(rawAnswer);
        if (m.find()) {
            try {
                return TicketStatus.valueOf(m.group(1));
            } catch (IllegalArgumentException ignored) {
                // tag presente mas valor desconhecido → fallback
            }
        }
        return TicketStatus.WAITING;
    }
}

