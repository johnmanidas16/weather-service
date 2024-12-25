package com.weather.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id), "RESOURCE_NOT_FOUND");
    }
}
