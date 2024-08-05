package com.example.exceptions;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException() {
        super();
    }
}
