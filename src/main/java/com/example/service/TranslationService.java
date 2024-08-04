package com.example.service;

import com.example.client.payload.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;

public interface TranslationService {

    Translation translate(String sourceLanguageCode, String targetLanguageCode, String text) throws InvalidLanguageCodeException, ProcessedSymbolsLimitException;
}
