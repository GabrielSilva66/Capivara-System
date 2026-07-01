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


    public List<Document> loadInternalKnowledge() {
        TikaDocumentReader reader = new TikaDocumentReader(internalKnowledge);
        return reader.read();
    }

    public List<Document> loadText(String filePath) {
        Resource resource = resolveResource(filePath);
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        return reader.read();
    }

    public List<Document> loadText(Resource resource) {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        return reader.read();
    }

    private Resource resolveResource(String path) {
        try {
            return new UrlResource(path);
        } catch (MalformedURLException e) {
            return new FileSystemResource(path);
        }
    }
}
