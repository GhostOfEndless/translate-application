package com.example.controller;

import com.example.client.payload.TranslationPayload;
import com.example.controller.payload.TranslationRequestPayload;
import com.example.entity.Translation;
import com.example.exceptions.InvalidLanguageCodeException;
import com.example.exceptions.ProcessedSymbolsLimitException;
import com.example.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("api/v1/translate")
@RequiredArgsConstructor
public class TranslationRestController {

    private final TranslationService translationService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<TranslationPayload> translateText(@Valid @RequestBody TranslationRequestPayload payload,
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
                request.getRemoteAddr(),
                Timestamp.from(Instant.now()),
                payload.sourceLanguageCode().toLowerCase(),
                payload.targetLanguageCode().toLowerCase(),
                payload.text());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{translationId}")
    public Translation findTranslation(@PathVariable(name = "translationId") Long translationId) {
        return this.translationService.findTranslation(translationId)
                .orElseThrow(() -> new NoSuchElementException("translation.not_found"));
    }

    @GetMapping("/page/{page}")
    public PagedModel<?> findAll(
            @PathVariable(name = "page") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "5") int pageSize) {
        Pageable pageable = PageRequest
                .of(pageNumber, pageSize, Sort.by("requestTimestamp")
                        .descending());
        return new PagedModel<>(this.translationService.findAllTranslations(pageable));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception,
                                                                      Locale locale) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                        this.messageSource.getMessage(exception.getMessage(), new Object[0],
                                exception.getMessage(), locale)));
    }
}
