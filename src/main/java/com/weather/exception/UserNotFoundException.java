package com.weather.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
	   public UserNotFoundException(String message) {
	       super(message);
	   }
	}
