package br.ufrn.imd.capivaai.module.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita o suporte a caching na aplicação.
 * Usado para cachear respostas de embeddings e resultados de busca semântica,
 * reduzindo latência e custo de chamadas à API de IA.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
