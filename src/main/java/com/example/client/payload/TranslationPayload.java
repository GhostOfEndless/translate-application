package com.example.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslationPayload(
        @JsonProperty("text")
        String text
) {
}
