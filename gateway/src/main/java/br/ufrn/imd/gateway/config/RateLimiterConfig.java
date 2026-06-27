package br.ufrn.imd.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean("globalKeyResolver")
    @Primary
    public KeyResolver globalKeyResolver() {
        return exchange -> Mono.just("GLOBAL_RATE_LIMIT_KEY");
    }
}
