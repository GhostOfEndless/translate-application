package com.example.service;

import com.example.client.YandexCloudRestClient;
import com.example.client.payload.Language;
import com.example.client.payload.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final YandexCloudRestClient restClient;
    private Set<String> availableLanguages;

    @PostConstruct
    private void init() {
        this.availableLanguages = new HashSet<>(
                this.restClient.getAvailableLanguages().stream().map(Language::code).toList()
        );
    }

    @Override
    public Translation translate(String sourceLanguageCode, String targetLanguageCode, String text)
            throws InvalidLanguageCodeException {
        if (!this.availableLanguages.contains(sourceLanguageCode)) {
            throw new InvalidLanguageCodeException(sourceLanguageCode);
        } else if (!this.availableLanguages.contains(targetLanguageCode)) {
            throw new InvalidLanguageCodeException(targetLanguageCode);
        }

        return this.restClient.translateText(sourceLanguageCode, targetLanguageCode, text);
    }
}
