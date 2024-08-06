package com.example.controller;

import com.example.BaseTest;
import com.example.client.payload.TranslationPayload;
import com.example.controller.payload.TranslationRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@RequiredArgsConstructor
class TranslationRestControllerTest extends BaseTest {

    private final MockMvc mockMvc;
    private final MessageSource messageSource;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    @Test
    void translateText_success() {
        String uri = "/api/v1/translate";

        String text = Stream.generate(() -> "one")
                .limit(20)
                .collect(Collectors.joining(" "));

        TranslationRequestPayload payload = TranslationRequestPayload.builder()
                .sourceLanguageCode("en")
                .targetLanguageCode("ru")
                .text(text)
                .build();

        var mvcResponse = mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        mvcResponse.setCharacterEncoding("UTF-8");
        String content = mvcResponse.getContentAsString();

        assertEquals(Stream.generate(() -> "один")
                .limit(20)
                .collect(Collectors.joining(" ")),
                objectMapper.readValue(content, TranslationPayload.class).text());
    }

    @SneakyThrows
    @Test
    void translateText_invalidLanguageCode() {
        String uri = "/api/v1/translate";

        var payload = TranslationRequestPayload.builder()
                .sourceLanguageCode("jk")
                .targetLanguageCode("ru")
                .text("Hello")
                .build();

        String content = mockBadRequestResponse(payload, uri).getContentAsString();

        assertTrue(content.contains(getErrorMessage("translation.request.invalid_language.code")
                .replace("{code}", "jk")));
    }

    @SneakyThrows
    @Test
    void translateText_blankBody() {
        String uri = "/api/v1/translate";

        String content = mockBadRequestResponse(null, uri).getContentAsString();

        assertTrue(content.contains(getErrorMessage("translation.request.body.is_null")));
    }

    @SneakyThrows
    @Test
    void translateText_blankSourceLanguageCode() {
        String uri = "/api/v1/translate";

        var payload = TranslationRequestPayload.builder()
                .targetLanguageCode("ru")
                .text("Hello")
                .build();

        String content = mockBadRequestResponse(payload, uri).getContentAsString();

        assertTrue(content.contains(getErrorMessage("translation.request.source_language_code.is_blank")));
    }

    @SneakyThrows
    @Test
    void translateText_blankTargetLanguageCode() {
        String uri = "/api/v1/translate";

        var payload = TranslationRequestPayload.builder()
                .sourceLanguageCode("en")
                .text("Hello")
                .build();

        String content = mockBadRequestResponse(payload, uri).getContentAsString();

        assertTrue(content.contains(getErrorMessage("translation.request.target_language_code.is_blank")));
    }

    @SneakyThrows
    @Test
    void translateText_blankText() {
        String uri = "/api/v1/translate";

        var payload = TranslationRequestPayload.builder()
                .sourceLanguageCode("en")
                .targetLanguageCode("ru")
                .build();

        String content = mockBadRequestResponse(payload, uri).getContentAsString();

        assertTrue(content.contains(getErrorMessage("translation.request.text.is_blank")));
    }

    @SneakyThrows
    @Test
    void translateText_invalidSizeSourceLanguageCode() {
        String uri = "/api/v1/translate";

        var payload = TranslationRequestPayload.builder()
                .sourceLanguageCode("e")
                .targetLanguageCode("ru")
                .text("hello")
                .build();

        String content = mockBadRequestResponse(payload, uri).getContentAsString();

        String errorMessage = getErrorMessage("translation.request.source_language_code.size_invalid")
                .replace("{max}", "\\d+")
                .replace("{min}", "\\d+");

        assertTrue(Pattern.compile(errorMessage).matcher(content).find());
    }

    @SneakyThrows
    @Test
    void translateText_invalidSizeTargetLanguageCode() {
        String uri = "/api/v1/translate";

        var payload = TranslationRequestPayload.builder()
                .sourceLanguageCode("en")
                .targetLanguageCode("rururururu")
                .text("hello")
                .build();

        String content = mockBadRequestResponse(payload, uri).getContentAsString();

        String errorMessage = getErrorMessage("translation.request.target_language_code.size_invalid")
                .replace("{max}", "\\d+")
                .replace("{min}", "\\d+");

        assertTrue(Pattern.compile(errorMessage).matcher(content).find());
    }

    @SneakyThrows
    private MockHttpServletResponse mockBadRequestResponse(TranslationRequestPayload payload, String uri) {
        var mvcResponse = mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn()
                .getResponse();

        mvcResponse.setCharacterEncoding("UTF-8");

        return mvcResponse;
    }

    private String getErrorMessage(String messageCode) {
        return Objects.requireNonNull(this.messageSource.getMessage(messageCode,
                new Object[0], messageCode, Locale.getDefault()));
    }
}
