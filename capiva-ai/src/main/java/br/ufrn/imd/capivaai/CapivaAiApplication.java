package br.ufrn.imd.capivaai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CapivaAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapivaAiApplication.class, args);
	}

	@Bean
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}
}
