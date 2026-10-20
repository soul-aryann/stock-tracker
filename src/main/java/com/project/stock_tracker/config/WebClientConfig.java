package com.project.stock_tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration 
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder, @Value("${alpha.vantage.base.url}") String baseUrl) {
        return builder.baseUrl(baseUrl != null ? baseUrl.trim() : "").build();
    }
}
