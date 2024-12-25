package com.weather.exception.handler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.dto.ApiError;
import com.weather.dto.ValidationError;
import com.weather.exception.DatabaseException;
import com.weather.exception.InvalidCredentialsException;
import com.weather.exception.InvalidTokenException;
import com.weather.exception.ResourceNotFoundException;
import com.weather.exception.WeatherServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * GlobalExceptionHandler is a centralized exception handling class for handling
 * exceptions that occur across the application. It uses Spring's
 * {@code @RestControllerAdvice} to intercept and handle exceptions in a
 * structured way, providing meaningful responses to the client with appropriate
 * HTTP status codes.
 * 
 * The class handles various types of exceptions including validation errors,
 * security-related errors, resource not found errors, service-level errors, and
 * unexpected internal errors. It also logs all exceptions for debugging
 * purposes.
 * 
 * <p>
 * Each exception handler method creates a standardized response using the
 * {@link ApiError} DTO. For validation errors, additional details are provided
 * through the {@link ValidationError} DTO.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Handles validation exceptions (e.g.,
 * {@link WebExchangeBindException}).</li>
 * <li>Handles security exceptions (e.g., {@link InvalidTokenException} and
 * {@link InvalidCredentialsException}).</li>
 * <li>Handles resource not found errors (e.g.,
 * {@link ResourceNotFoundException}).</li>
 * <li>Handles service-related errors (e.g., {@link DatabaseException} and
 * {@link WeatherServiceException}).</li>
 * <li>Handles external service errors (e.g.,
 * {@link WebClientResponseException}).</li>
 * <li>Handles unexpected internal errors (e.g., generic
 * {@link Exception}).</li>
 * </ul>
 * 
 * <p>
 * All exception responses include the following details:
 * </p>
 * <ul>
 * <li>Timestamp of the error.</li>
 * <li>HTTP status code.</li>
 * <li>Error message and detailed description.</li>
 * <li>Request path where the error occurred.</li>
 * <li>Unique trace ID for debugging purposes.</li>
 * </ul>
 * 
 * <p>
 * This class ensures consistent error handling and logging throughout the
 * application.
 * </p>
 * 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    
    private final ObjectMapper objectMapper;
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Set default status code
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = UNEXPECTED_ERROR;
        String error = "Internal Server Error";
        List<ValidationError> validationErrors = Collections.emptyList();

        // Determine the appropriate status and message based on exception type
        if (ex instanceof InvalidTokenException || ex instanceof InvalidCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            message = ex.getMessage();
            error = "Authentication Failed";
        } 
        else if (ex instanceof WebExchangeBindException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Validation failed";
            error = "Invalid Request";
            validationErrors = extractValidationErrors((WebExchangeBindException) ex);
        }
        else if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = ex.getMessage();
            error = "Resource Not Found";
        }
        else if (ex instanceof WebClientResponseException) {
            WebClientResponseException wcEx = (WebClientResponseException) ex;
            status = HttpStatus.valueOf(wcEx.getStatusCode().value());
            message = wcEx.getMessage();
            error = "External Service Error";
        }
        else if (ex instanceof DatabaseException || ex instanceof WeatherServiceException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable";
            error = "Service Error";
        }
        else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseError = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(responseError.getStatusCode().value());  // Convert HttpStatusCode to HttpStatus
            message = responseError.getReason();
            error = "Request Error";
        }
       
        // Log the error
        logError(error, ex);

        // Build and return the error response
        return writeErrorResponse(exchange, buildErrorResponse(
            status,
            error,
            message,
            exchange.getRequest().getPath().value(),
            validationErrors
        ));
    }

    private List<ValidationError> extractValidationErrors(WebExchangeBindException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
    }

    private ApiError buildErrorResponse(HttpStatus status, String error, String message, 
                                     String path, List<ValidationError> validationErrors) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .errors(validationErrors)
                .traceId(UUID.randomUUID().toString())
                .build();
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, ApiError errorResponse) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(errorResponse.getStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        try {
            DataBuffer buffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            return Mono.error(e);
        }
    }

    private void logError(String message, Throwable ex) {
        if (ex instanceof ResourceNotFoundException || ex instanceof InvalidTokenException) {
            log.warn("{} - Type: [{}] - Message: [{}]", message, ex.getClass().getSimpleName(), ex.getMessage());
        } else {
            log.error("{} - Type: [{}] - Message: [{}]", message, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }
}