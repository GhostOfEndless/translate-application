package com.example.service;

import com.example.client.payload.Translation;
import com.example.exceptions.InvalidLanguageCodeException;

public interface TranslationService {

    Translation translate(String sourceLanguageCode, String targetLanguageCode, String text) throws InvalidLanguageCodeException;
}
