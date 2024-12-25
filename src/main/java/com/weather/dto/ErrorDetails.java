package com.weather.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorDetails implements Serializable {
  
	private static final long serialVersionUID = 49215717810366461L;
	
	private final HttpStatus status;
    private final String message;
    private final String error;
    private final List<ValidationError> validationErrors;
}
