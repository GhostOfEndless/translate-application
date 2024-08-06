package com.example.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TranslationRequestPayload(
        @JsonProperty("sourceLanguageCode")
        @NotBlank(message = "{translation.request.source_language_code.is_blank}")
        @Size(min = 2, max = 7, message = "{translation.request.source_language_code.size_invalid}")
        String sourceLanguageCode,

        @JsonProperty("targetLanguageCode")
        @NotBlank(message = "{translation.request.target_language_code.is_blank}")
        @Size(min = 2, max = 7, message = "{translation.request.target_language_code.size_invalid}")
        String targetLanguageCode,

        @JsonProperty("text")
        @NotBlank(message = "{translation.request.text.is_blank}")
        String text
) {
}
