package com.example.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AvailableLanguagesResponsePayload(
        @JsonProperty("languages")
        List<LanguagePayload> languages
) {
}
