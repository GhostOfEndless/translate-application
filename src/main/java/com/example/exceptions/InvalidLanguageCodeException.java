package com.example.exceptions;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class InvalidLanguageCodeException extends Exception {

    public InvalidLanguageCodeException(String message) {
        super(message);
    }
}
