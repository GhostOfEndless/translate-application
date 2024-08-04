package com.example.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ProcessedSymbolsLimitException extends Exception {

    private final int symbolsLimit;

    public ProcessedSymbolsLimitException(int symbolsLimit) {
        super();
        this.symbolsLimit = symbolsLimit;
    }
}
