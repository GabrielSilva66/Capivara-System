package br.ufrn.imd.capivaai.module.ai.config;

import br.ufrn.imd.capivaai.module.ai.tools.ticket.SupportTicketTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
public class SupportTecClientConfig {

    private static final int MEMORY_WINDOW_SIZE = 20;

    private static final Set<String> ALLOWED_GITHUB_TOOLS = Set.of(
            "list_issues",
            "create_issue",
            "get_issue",
            "add_issue_comment"
    );

    @Value("classpath:prompts/suporte-tecnico-prompt.txt")
    private Resource systemPrompt;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(MEMORY_WINDOW_SIZE)
                .build();
    }

    @Bean("supportTecChatClient")
    public ChatClient supportTecChatClient(
            ChatClient.Builder builder,
            SupportTicketTools ticketTools,
            @Autowired(required = false) List<SyncMcpToolCallbackProvider> mcpProviders) throws IOException {

        var b = builder
                .defaultSystem(loadPrompt(systemPrompt))
                .defaultTools(ticketTools);

        if (mcpProviders != null) {
            ToolCallback[] filtered = mcpProviders.stream()
                    .flatMap(p -> Arrays.stream(p.getToolCallbacks()))
                    .filter(t -> ALLOWED_GITHUB_TOOLS.contains(t.getToolDefinition().name()))
                    .toArray(ToolCallback[]::new);
            b.defaultToolCallbacks(filtered);
        }

        return b.build();
    }

    private String loadPrompt(Resource resource) throws IOException {
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
