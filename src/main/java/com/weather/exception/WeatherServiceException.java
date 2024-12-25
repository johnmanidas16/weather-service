package com.weather.exception;

import lombok.Getter;

@Getter
public class WeatherServiceException extends InfrastructureException {
    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}