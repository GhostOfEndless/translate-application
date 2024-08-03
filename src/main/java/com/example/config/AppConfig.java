package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

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
}
