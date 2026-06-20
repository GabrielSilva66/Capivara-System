package br.ufrn.imd.capivaai.module.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Configuration
public class SupportTecClientConfig {

    private static final int MEMORY_WINDOW_SIZE = 3;

    @Value("classpath:prompts/suporte-tecnico-prompt.txt")
    private Resource systemPrompt;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(MEMORY_WINDOW_SIZE)
                .build();
    }

    @Bean("supportTecChatClient")
    public ChatClient supportTecChatClient(ChatClient.Builder builder) throws IOException {
        return builder
                .defaultSystem(loadPrompt(systemPrompt))
                .build();
    }

    private String loadPrompt(Resource resource) throws IOException {
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
