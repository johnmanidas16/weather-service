package com.weather.exception;

import lombok.Getter;

@Getter
public class InfrastructureException extends BaseException {
    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
