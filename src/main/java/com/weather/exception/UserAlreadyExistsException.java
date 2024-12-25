package com.weather.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String username) {
        super(String.format("User already exists with username: %s", username), "USER_EXISTS");
    }
}
