package com.weather.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError implements Serializable {
	
	private static final long serialVersionUID = 3389493195506504821L;
	
	private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<ValidationError> errors;
    private final String traceId;
}