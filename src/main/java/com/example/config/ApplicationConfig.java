package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public String apiKey(@Value("${yandex.cloud.api-key}") String apiKey) {
        return apiKey;
    }

    @Bean
    public String apiEndpoint(@Value("${yandex.cloud.api-endpoint}") String apiEndpoint) {
        return apiEndpoint;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Integer requestsLimit(@Value("${yandex.cloud.requests-limit}") Integer requestsLimit) {
        return requestsLimit;
    }

    @Bean
    public Integer symbolsLimit(@Value("${yandex.cloud.symbols-limit}") Integer symbolsLimit) {
        return symbolsLimit;
    }

    @Bean
    public Integer translationPoolThreadsNum(@Value("${translation.service.threads}") Integer translationPoolThreads) {
        return translationPoolThreads;
    }
}
