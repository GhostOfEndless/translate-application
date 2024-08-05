package com.example.client.payload;

import java.util.List;

public record TranslationResponsePayload(List<TranslationPayload> translations) {
}
