package com.weather.exception;

import lombok.Getter;

@Getter
public class InvalidTokenException extends SecurityException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
