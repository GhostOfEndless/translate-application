package com.example.client.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LanguagePayload(
        @JsonProperty("code")
        String code
) { }
