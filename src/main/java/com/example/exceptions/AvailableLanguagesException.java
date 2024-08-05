package com.example.exceptions;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class AvailableLanguagesException extends RuntimeException {

    public AvailableLanguagesException() {
        super();
    }
}
