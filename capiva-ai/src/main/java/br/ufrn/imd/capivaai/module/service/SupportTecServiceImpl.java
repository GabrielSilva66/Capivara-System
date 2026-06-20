package br.ufrn.imd.capivaai.module.service;

import br.ufrn.imd.capivaai.domain.repository.SupportTecRepository;
import br.ufrn.imd.capivaai.domain.service.SupportTecService;
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
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class SupportTecServiceImpl implements SupportTecService {

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
        this.repository  = repository;
        this.chatClient  = chatClient;
        this.chatMemory  = chatMemory;
        this.vectorStore = vectorStore;
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
                .map(doc -> doc.getText())
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
    public String ask(String conversationId, String userMessage) {
        var responseFlux = chatClient.prompt()
                .advisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userMessage)
                .stream()
                .content();

        String fullText = responseFlux
                .collectList()
                .map(list -> String.join("", list))
                .block();

        if (fullText != null) {

            fullText = fullText.replace("\\n", "\n").replace("\\r", "\r");
        }

        System.out.println("[Groq + MCP Response]: " + fullText);

        return fullText;
    }
}
