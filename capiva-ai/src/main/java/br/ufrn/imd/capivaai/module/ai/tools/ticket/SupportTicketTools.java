package br.ufrn.imd.capivaai.module.ai.tools.ticket;

import br.ufrn.imd.capivaai.domain.repository.SupportTecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class SupportTicketTools {

    private final SupportTecRepository repository;

    @Tool(description = """
            Busca na base de conhecimento técnica interna soluções e procedimentos
            relacionados ao problema descrito. Retorna os trechos mais relevantes
            encontrados na documentação catalogada.
            Chame ANTES de escalar para o GitHub, se encontrar solução, resolva
            diretamente sem criar issue.
            """)
    public String searchKnowledgeBase(
            @ToolParam(description = "Descrição do problema ou palavras-chave para busca") String query,
            @ToolParam(description = "Número máximo de resultados (1 a 5)") int maxResults) {

        int limit = Math.max(1, Math.min(maxResults, 5));
        List<String> matches = repository.findClosestMatches(query, limit);

        if (matches.isEmpty()) {
            return "Nenhum documento relevante encontrado na base de conhecimento para: \"" + query + "\"";
        }

        StringBuilder sb = new StringBuilder("Resultados encontrados na base de conhecimento:\n\n");
        for (int i = 0; i < matches.size(); i++) {
            sb.append("--- Documento ").append(i + 1).append(" ---\n");
            sb.append(matches.get(i)).append("\n\n");
        }
        return sb.toString();
    }
}
