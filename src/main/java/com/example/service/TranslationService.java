package com.example.service;

import com.example.client.payload.TranslationPayload;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;

import java.sql.Timestamp;

public interface TranslationService {

    TranslationPayload translate(String clientIP, Timestamp requestTimestamp, String sourceLanguageCode,
                                 String targetLanguageCode, String sourceText)
            throws InvalidLanguageCodeException, ProcessedSymbolsLimitException;
}
