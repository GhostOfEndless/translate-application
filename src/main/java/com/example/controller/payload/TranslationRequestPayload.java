package com.example.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TranslationRequestPayload(
        @NotBlank(message = "{translation.request.source_language_code.is_blank}")
        @Size(min = 2, max = 7, message = "{translation.request.source_language_code.size_invalid}")
        String sourceLanguageCode,

        @NotBlank(message = "{translation.request.target_language_code.is_blank}")
        @Size(min = 2, max = 7, message = "{translation.request.target_language_code.size_invalid}")
        String targetLanguageCode,

        @NotBlank(message = "{translation.request.text.is_blank}")
        @Size(max = 10_000, message = "{translation.request.text.size_invalid}")
        String text
) {
}
