package com.example.client;

import com.example.client.payload.AvailableLanguagesResponsePayload;
import com.example.client.payload.LanguagePayload;
import com.example.client.payload.TranslationResponsePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YandexCloudRestClientImpl implements YandexCloudRestClient {

    private final String apiKey;
    private final String apiEndpoint;
    private final RestTemplate restTemplate;

    @Override
    public String translateText(String sourceLanguageCode, String targetLanguageCode, String text) {
        var requestBody = new HashMap<>();
        requestBody.put("sourceLanguageCode", sourceLanguageCode);
        requestBody.put("targetLanguageCode", targetLanguageCode);
        requestBody.put("texts", new ArrayList<>() {{
            add(text);
        }});
        var headers = new HttpHeaders();
        headers.add("Authorization", "Api-Key " + this.apiKey);
        var request = new HttpEntity<>(requestBody, headers);

        var response = this.restTemplate.postForEntity(this.apiEndpoint + "/translate", request,
                TranslationResponsePayload.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                && response.getBody().translations() != null) {
            return response.getBody().translations().getFirst().text();
        }

        return null;
    }

    @Override
    public List<LanguagePayload> getAvailableLanguages() {
        var headers = new HttpHeaders();
        headers.add("Authorization", "Api-Key " + this.apiKey);
        var request = new HttpEntity<>(headers);

        var response = this.restTemplate.postForEntity(this.apiEndpoint + "/languages", request,
                AvailableLanguagesResponsePayload.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                && response.getBody().languages() != null) {
            return response.getBody().languages();
        }

        return new ArrayList<>();
    }
}
