package com.example.service;

import com.example.client.payload.TranslationPayload;
import com.example.entity.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.Optional;

public interface TranslationService {

    TranslationPayload translate(String clientIP, Timestamp requestTimestamp, String sourceLanguageCode,
                                 String targetLanguageCode, String sourceText)
            throws InvalidLanguageCodeException, ProcessedSymbolsLimitException;

    Optional<Translation> findTranslation(Long id);

    Page<Translation> findAllTranslations(Pageable pageable);
}
