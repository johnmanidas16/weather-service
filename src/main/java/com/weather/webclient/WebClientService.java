package com.weather.webclient;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.weather.exception.ApiClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Service to handle HTTP requests using WebClient with retry mechanism.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebClientService {

	public static final String EXTERNAL_SERVICE_FAILED_TO_PROCESS_AFTER_MAX_RETRIES = "External Service failed to process after max retries";
	public static final String IDENTITY_SERVICE_UNAVAILABLE_AFTER_MAX_RETRIES = "External Service unavailable after max retries";
	public static final int MAX_ATTEMPTS = 3;
	public static final int SECONDS = 1;

	/**
	 * Creates a WebClient instance for a given base URL.
	 *
	 * @param baseUrl The base URL for the WebClient.
	 * @return A WebClient instance configured with the provided base URL.
	 */
	private WebClient webClientBuilder(String baseUrl) {
		WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
		return builder.build();
	}

	/**
	 * Executes an HTTP request with retry mechanism.
	 *
	 * @param <T>          The type of the response object.
	 * @param baseUrl      The base URL of the external service.
	 * @param uri          The URI for the request.
	 * @param traceId      A trace ID for logging purposes.
	 * @param httpMethod   The HTTP method to be used (e.g., GET, POST).
	 * @param responseType The expected response type.
	 * @return A Mono containing the response object.
	 */
	public <T> Mono<T> executeRequest(String baseUrl, String uri, String traceId, HttpMethod get,
			Class<T> responseType) {
		return Mono.defer(() -> {
			return webClientBuilder(baseUrl)
					.method(get)
					.uri(uri)
					.accept(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.retrieve()
					.bodyToMono(responseType);
		}).retryWhen(Retry.backoff(MAX_ATTEMPTS, Duration.ofSeconds(SECONDS))
				.filter(this::filterThrowable)
				.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> onRetryExhaustedThrow(retrySignal)));
	}

	/**
	 * Filters the throwable to determine whether it should be retried.
	 *
	 * @param throwable The exception thrown during the request.
	 * @return True if the exception is retryable (e.g., UNAUTHORIZED), false otherwise.
	 */
	private boolean filterThrowable(Throwable throwable) {
		if (throwable instanceof WebClientResponseException webClientException) {
			return webClientException.getStatusCode() == HttpStatus.UNAUTHORIZED;
		}
		return false;
	}

	/**
	 * Handles the logic when retries are exhausted.
	 *
	 * @param retrySignal The retry signal containing information about the failure.
	 * @return A Throwable to be thrown after retries are exhausted.
	 */
	private Throwable onRetryExhaustedThrow(Retry.RetrySignal retrySignal) {
		Throwable failure = retrySignal.failure();
		if (failure instanceof WebClientResponseException webClientException) {
			if (webClientException.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				throw new ApiClientException(EXTERNAL_SERVICE_FAILED_TO_PROCESS_AFTER_MAX_RETRIES,
						HttpStatus.UNAUTHORIZED.value(), 
						failure);
			}
		}
		return failure;
	}
}
