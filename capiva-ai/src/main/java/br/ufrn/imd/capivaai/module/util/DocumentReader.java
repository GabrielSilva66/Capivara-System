package br.ufrn.imd.capivaai.module.util;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.List;


@Component
public class DocumentReader {


    @Value("${app.knowledge-base.path:classpath:knowledge/knowledge-base.txt}")
    private Resource internalKnowledge;

    /**
     * Lê o arquivo interno de base de conhecimento configurado em
     * {@code app.knowledge-base.path}.
     * <p>
     *
     * @return lista de {@link Document} extraídos pelo Tika
     */
    public List<Document> loadInternalKnowledge() {
        TikaDocumentReader reader = new TikaDocumentReader(internalKnowledge);
        return reader.read();
    }

    /**
     * Lê documentos de um caminho local ou URL.
     *
     * @param filePath caminho absoluto ou URL (http/https/file://)
     * @return lista de {@link Document} extraídos pelo Tika
     */
    public List<Document> loadText(String filePath) {
        Resource resource = resolveResource(filePath);
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        return reader.read();
    }

    /**
     * Lê documentos diretamente de um {@link Resource} Spring.
     *
     * @param resource resource a ser lido
     * @return lista de {@link Document} extraídos pelo Tika
     */
    public List<Document> loadText(Resource resource) {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        return reader.read();
    }

    /**
     * Resolve o {@link Resource} a partir de um caminho ou URL.
     * Tenta primeiro como URL (http/https/file://), depois como caminho local.
     */
    private Resource resolveResource(String path) {
        try {
            return new UrlResource(path);
        } catch (MalformedURLException e) {
            return new FileSystemResource(path);
        }
    }
}
