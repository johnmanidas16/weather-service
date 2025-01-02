package com.weather.exception.handler;

import com.weather.dto.ApiError;
import com.weather.dto.ValidationError;
import com.weather.utils.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ControllerAdviceException {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidationErrors(WebExchangeBindException ex, ServerWebExchange exchange) {
        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ErrorConstants.INVALID_REQUEST)
                .message(ErrorConstants.VALIDATION_FAILED)
                .path(exchange.getRequest().getPath().value())
                .errors(validationErrors)
                .traceId(UUID.randomUUID().toString())
                .build();

        log.warn("Validation error occurred - Type: [{}] - Message: [{}]",
                ex.getClass().getSimpleName(),
                ex.getMessage());

        return Mono.just(ResponseEntity.badRequest().body(apiError));
    }
}
