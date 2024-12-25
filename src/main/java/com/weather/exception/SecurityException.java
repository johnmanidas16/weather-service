package com.weather.exception;

import lombok.Getter;

@Getter
public class SecurityException extends BaseException {
    public SecurityException(String message) {
        super(message);
    }
}
