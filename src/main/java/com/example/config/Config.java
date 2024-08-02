package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class Config {

    @Bean
    public String yandexCloudApiKey(@Value("${yandex.cloud.api-key}") String apiKey) {
        log.info("Yandex Cloud API Key: {}", apiKey);
        return apiKey;
    }
}
