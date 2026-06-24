package br.ufrn.imd.capivaai.module.service;

import br.ufrn.imd.capivaai.domain.repository.SupportTecRepository;
import br.ufrn.imd.capivaai.module.util.DocumentReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseLoader {

    private final DocumentReader documentReader;
    private final SupportTecRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[KnowledgeBaseLoader] Iniciando ingestão da base de conhecimento interna...");
        try {
            List<Document> documents = documentReader.loadInternalKnowledge();
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunkDocs = splitter.apply(documents);
            repository.addChunks(chunkDocs);
            log.info("[KnowledgeBaseLoader] Base de conhecimento carregada com sucesso na VectorStore.");
        } catch (Exception e) {
            log.error("[KnowledgeBaseLoader] Falha ao carregar a base de conhecimento: {}", e.getMessage(), e);
        }
    }
}
