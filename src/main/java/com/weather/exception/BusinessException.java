package com.weather.exception;

import lombok.Getter;

@Getter
public class BusinessException extends BaseException {
    private final String errorCode;
    
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
