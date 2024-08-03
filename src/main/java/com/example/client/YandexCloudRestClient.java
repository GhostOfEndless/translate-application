package com.example.client;

import com.example.client.payload.Language;
import com.example.client.payload.Translation;

import java.util.List;

public interface YandexCloudRestClient {

    Translation translateText(String sourceLanguageCode, String targetLanguageCode, String text);

    List<Language> getAvailableLanguages();
}
