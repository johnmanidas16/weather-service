package com.weather.exception.handler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import com.weather.exception.*;
import com.weather.utils.ErrorConstants;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.dto.ApiError;
import com.weather.dto.ValidationError;

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

    @Getter
    @Builder
    private static class ErrorDetails {
        private final HttpStatus status;
        private final String message;
        private final String error;
        private final List<ValidationError> validationErrors;
    }

    // Registry of exception handlers
    private final Map<Class<? extends Throwable>, Function<Throwable, ErrorDetails>> errorHandlers = createErrorHandlers();

    private Map<Class<? extends Throwable>, Function<Throwable, ErrorDetails>> createErrorHandlers() {
        Map<Class<? extends Throwable>, Function<Throwable, ErrorDetails>> handlers = new HashMap<>();

        handlers.put(InvalidTokenException.class, ex -> ErrorDetails.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(ex.getMessage())
                .error(ErrorConstants.AUTHENTICATION_FAILED)
                .validationErrors(Collections.emptyList())
                .build());

        handlers.put(InvalidCredentialsException.class, ex -> ErrorDetails.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(ex.getMessage())
                .error(ErrorConstants.AUTHENTICATION_FAILED)
                .validationErrors(Collections.emptyList())
                .build());

        handlers.put(UnauthorizedAccessException.class, ex -> ErrorDetails.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(ex.getMessage())
                .error(ErrorConstants.AUTHORIZATION_FAILED)
                .validationErrors(Collections.emptyList())
                .build());

        handlers.put(ValidationException.class, ex -> ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .error(ErrorConstants.INVALID_REQUEST)
                .validationErrors(Collections.emptyList())
                .build());

        handlers.put(ResourceNotFoundException.class, ex -> ErrorDetails.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(ex.getMessage())
                .error(ErrorConstants.RESOURCE_NOT_FOUND)
                .validationErrors(Collections.emptyList())
                .build());

        handlers.put(WebClientResponseException.class, ex -> {
            WebClientResponseException wcEx = (WebClientResponseException) ex;
            return ErrorDetails.builder()
                    .status(HttpStatus.valueOf(wcEx.getStatusCode().value()))
                    .message(wcEx.getMessage())
                    .error(ErrorConstants.EXTERNAL_SERVICE_ERROR)
                    .validationErrors(Collections.emptyList())
                    .build();
        });

        // Service Errors
        Stream.of(DatabaseException.class, WeatherServiceException.class)
                .forEach(exceptionClass -> handlers.put(exceptionClass, ex -> ErrorDetails.builder()
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .message(ErrorConstants.SERVICE_TEMPORARILY_UNAVAILABLE)
                        .error(ErrorConstants.SERVICE_ERROR)
                        .validationErrors(Collections.emptyList())
                        .build()));

        return Collections.unmodifiableMap(handlers);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ErrorDetails errorDetails = errorHandlers.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(ex))
                .findFirst()
                .map(entry -> entry.getValue().apply(ex))
                .orElseGet(() -> getDefaultErrorDetails(ex));

        logError(errorDetails.getError(), ex);
        return writeErrorResponse(exchange, buildApiError(exchange, errorDetails));
    }

    private ErrorDetails getDefaultErrorDetails(Throwable ex) {
        return ErrorDetails.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(ErrorConstants.UNEXPECTED_ERROR)
                .error(ErrorConstants.INTERNAL_SERVER_ERROR)
                .validationErrors(Collections.emptyList())
                .build();
    }

    private ApiError buildApiError(ServerWebExchange exchange, ErrorDetails errorDetails) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(errorDetails.getStatus().value())
                .error(errorDetails.getError())
                .message(errorDetails.getMessage())
                .path(exchange.getRequest().getPath().value())
                .errors(errorDetails.getValidationErrors())
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