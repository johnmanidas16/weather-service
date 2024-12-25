package com.weather.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationError implements Serializable {
	
	private static final long serialVersionUID = 8656743408619440096L;
	
	private final String field;
	private final Object rejectedValue;
	private final String message;
}
