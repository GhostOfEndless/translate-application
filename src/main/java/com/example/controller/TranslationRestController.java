package com.example.controller;

import com.example.client.payload.Translation;
import com.example.controller.payload.TranslationRequestPayload;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;
import com.example.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("api/v1/translate")
@RequiredArgsConstructor
public class TranslationRestController {

    private final TranslationService translationService;

    @PostMapping
    public ResponseEntity<Translation> translateText(@Valid @RequestBody TranslationRequestPayload payload,
                                                     BindingResult bindingResult,
                                                     HttpServletRequest request)
            throws BindException, InvalidLanguageCodeException, ProcessedSymbolsLimitException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        }

        var response = this.translationService.translate(
                payload.sourceLanguageCode().toLowerCase(),
                payload.targetLanguageCode().toLowerCase(),
                payload.text());

        log.info("Request body: {}", payload);
        log.info("Client IP is: {}", request.getRemoteAddr());
        log.info("Current time is: {}", Timestamp.from(Instant.now()));
        log.info("Response from YC: {}", response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
