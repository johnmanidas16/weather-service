package com.weather.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
