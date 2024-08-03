package com.example.controller;

import com.example.exceptions.InvalidLanguageCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;
import java.util.Objects;

@ControllerAdvice
@RequiredArgsConstructor
public class BadRequestControllerAdvice {

    private final MessageSource messageSource;

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception, Locale locale) {
        var problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        this.messageSource.getMessage("errors.400.title", new Object[0],
                                "errors.400.title", locale));
        problemDetail.setProperty("errors",
                exception.getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .toList());

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleParseException(HttpMessageNotReadableException exception,
                                                              Locale locale) {
        var problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        this.messageSource.getMessage("errors.400.title", new Object[0],
                                "errors.400.title", locale));

        var errorMessage = exception.getMessage().startsWith("Required request body is missing") ?
                this.messageSource.getMessage("translation.request.body.is_null", new Object[0],
                        "translation.request.body.is_null", locale) : exception.getMessage();

        problemDetail.setProperty("error", errorMessage);

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(InvalidLanguageCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidLanguageCodeException(InvalidLanguageCodeException exception,
                                                                            Locale locale) {
        var problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        this.messageSource.getMessage("errors.400.title", new Object[0],
                                "errors.400.title", locale));

        var errorMessage = Objects.requireNonNull(this.messageSource
                        .getMessage("translation.request.invalid_language.code", new Object[0],
                                "translation.request.invalid_language.code", locale))
                .replace("{code}", exception.getMessage());

        problemDetail.setProperty("error", errorMessage);

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }
}
