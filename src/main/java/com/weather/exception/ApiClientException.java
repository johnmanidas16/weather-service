package com.weather.exception;

import lombok.Getter;


@Getter
public class ApiClientException extends InfrastructureException {
    private final int statusCode;
    
    public ApiClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
