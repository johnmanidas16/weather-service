package com.weather.exception;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends SecurityException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
