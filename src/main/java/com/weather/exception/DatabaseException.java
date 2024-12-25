package com.weather.exception;

import lombok.Getter;

@Getter
public class DatabaseException extends InfrastructureException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
