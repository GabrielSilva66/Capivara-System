package br.ufrn.imd.capivaai.module.controller;

import br.ufrn.imd.capivaai.domain.model.AskResponse;
import br.ufrn.imd.capivaai.domain.service.SupportTecService;
import br.ufrn.imd.capivaai.module.dto.request.AskInputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SupportTecGraphQLController {

    private final SupportTecService supportTecService;

    @QueryMapping
    public List<String> findKnowledgeMatches(
            @Argument String query,
            @Argument int topK) {
        return supportTecService.findClosestMatches(query, topK);
    }

    @MutationMapping
    public AskResponse ask(@Argument AskInputDTO input) {
        return supportTecService.ask(input);
    }

    @MutationMapping
    public boolean ingestKnowledge(@Argument List<String> texts) {
        supportTecService.add(texts);
        return true;
    }

    @MutationMapping
    public boolean ingestDocument(@Argument String fileUrl) {
        supportTecService.ingestDocument(fileUrl);
        return true;
    }
}

