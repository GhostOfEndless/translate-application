package com.example.client;

import com.example.client.payload.LanguagePayload;

import java.util.List;

public interface YandexCloudRestClient {

    String translateText(String sourceLanguageCode, String targetLanguageCode, String text);

    List<LanguagePayload> getAvailableLanguages();
}
