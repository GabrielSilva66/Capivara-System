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

/**
 * Carrega a base de conhecimento interna na VectorStore logo após o
 * contexto Spring estar completamente inicializado.
 * <p>
 * O evento {@link ApplicationReadyEvent} garante que todos os beans —
 * incluindo o {@link org.springframework.ai.vectorstore.VectorStore} e o
 * modelo de embedding — estão prontos antes da ingestão começar.
 * <p>
 * O arquivo lido é configurado em {@code app.knowledge-base.path}
 * (padrão: {@code classpath:knowledge/knowledge-base.txt}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseLoader {

    private final DocumentReader documentReader;
    private final SupportTecRepository repository;

    /**
     * Dispara a ingestão da base de conhecimento após o boot completo.
     * Em caso de falha, loga o erro sem derrubar a aplicação — o serviço
     * sobe normalmente, mas sem o contexto pré-carregado.
     */
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
