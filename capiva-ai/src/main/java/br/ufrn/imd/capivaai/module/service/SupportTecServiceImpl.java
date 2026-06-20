package br.ufrn.imd.capivaai.module.service;

import br.ufrn.imd.capivaai.domain.repository.SupportTecRepository;
import br.ufrn.imd.capivaai.domain.service.SupportTecService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTecServiceImpl implements SupportTecService {

    private final SupportTecRepository repository;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;

    public SupportTecServiceImpl(
            SupportTecRepository repository,
            @Qualifier("supportTecChatClient") ChatClient chatClient,
            ChatMemory chatMemory,
            VectorStore vectorStore) {
        this.repository  = repository;
        this.chatClient  = chatClient;
        this.chatMemory  = chatMemory;
        this.vectorStore = vectorStore;
    }

    @Override
    public void add(List<String> information) {
        repository.add(information);
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
        return chatClient.prompt()
                .advisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()

                )
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userMessage)
                .call()
                .content();
    }
}
