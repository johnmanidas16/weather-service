package com.weather.exception;

import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends SecurityException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}

