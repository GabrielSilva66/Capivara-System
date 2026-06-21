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
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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

    // ──────────────────────────────────────────────────────────────
    // Ingestão de conhecimento
    // ──────────────────────────────────────────────────────────────

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

    // ──────────────────────────────────────────────────────────────
    // Busca semântica
    // ──────────────────────────────────────────────────────────────

    @Override
    public List<String> findClosestMatches(String query, int numberOfMatches) {
        return repository.findClosestMatches(query, numberOfMatches);
    }

    @Override
    public String findCloseMatch(String query) {
        return repository.findCloseMatch(query);
    }

    // ──────────────────────────────────────────────────────────────
    // Chat principal
    // ──────────────────────────────────────────────────────────────

    /**
     * Processa a mensagem do usuário com RAG + memória + ferramentas MCP.
     *
     * <p>Os dados estruturados do formulário ({@code userName}, {@code title},
     * {@code description}, {@code severity}) são injetados como um bloco de
     * contexto antes da mensagem livre do usuário. Isso permite que a IA use
     * essas informações diretamente ao chamar {@code createTicket} e
     * {@code create_issue} — sem precisar perguntar ao usuário durante o chat.
     *
     * <p>A IA emite ao final da resposta uma tag invisível no formato
     * {@code [STATUS:RESOLVED|WAITING|ESCALATED_GITHUB]}. Este método
     * extrai essa tag, mapeia para o enum {@link TicketStatus} e a remove
     * do texto retornado ao usuário.
     */
    @Override
    public AskResponse ask(AskInputDTO input) {

        // 1. Monta prompt enriquecido com contexto do formulário
        String enrichedMessage = buildEnrichedMessage(input);

        // 2. Chama a IA com RAG + memória
        String rawAnswer = chatClient.prompt()
                .advisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, input.getConversationId()))
                .user(enrichedMessage)
                .stream()
                .content()
                .collectList()
                .map(chunks -> String.join("", chunks))
                .block();

        if (rawAnswer == null) rawAnswer = "";

        // 3. Normaliza quebras de linha escapadas
        rawAnswer = rawAnswer.replace("\\n", "\n").replace("\\r", "\r");

        // 4. Extrai tag [STATUS:*] e determina o ticketStatus
        TicketStatus ticketStatus = extractStatus(rawAnswer);

        // 5. Remove a tag do texto final (não deve aparecer ao usuário)
        String cleanAnswer = STATUS_PATTERN.matcher(rawAnswer).replaceAll("").strip();

        System.out.printf("[capiva-ai] status=%s | answer=%s%n", ticketStatus, cleanAnswer);

        return new AskResponse(cleanAnswer, ticketStatus);
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────

    /**
     * Injeta os dados estruturados como bloco de contexto antes da mensagem
     * livre do usuário, garantindo que a IA tenha todas as informações para
     * preencher o ticket e a issue sem interações adicionais.
     */
    private String buildEnrichedMessage(AskInputDTO input) {
        return """
                [CONTEXTO DO CHAMADO]
                Usuário: %s
                Título: %s
                Descrição: %s
                Severidade: %s

                [MENSAGEM DO USUÁRIO]
                %s
                """.formatted(
                input.getUserName(),
                input.getTitle(),
                input.getDescription(),
                input.getSeverity(),
                input.getMessage()
        );
    }

    /**
     * Extrai o {@link TicketStatus} da tag {@code [STATUS:*]} presente na
     * resposta bruta da IA. Retorna {@link TicketStatus#WAITING} como
     * fallback seguro caso a tag esteja ausente ou inválida.
     */
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

