package com.weather.security;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.dto.ApiError;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * JwtAuthenticationFilter is a custom implementation of {@link WebFilter} that
 * intercepts HTTP requests to validate JWT-based authentication. It uses
 * {@link JwtAuthenticationManager} to authenticate incoming requests based on
 * the provided JWT token in the Authorization header.
 * 
 * <p>
 * This filter ensures that only authenticated users can access protected
 * routes, while skipping authentication for certain predefined paths such as
 * API documentation or public authentication endpoints.
 * </p>
 * 
 * <p>
 * Key Features:
 * </p>
 * <ul>
 * <li>Skips authentication for specific paths, including Swagger docs and
 * authentication endpoints.</li>
 * <li>Extracts the Bearer token from the Authorization header.</li>
 * <li>Authenticates the extracted JWT token using
 * {@link JwtAuthenticationManager}.</li>
 * <li>Sets the {@link SecurityContext} upon successful authentication.</li>
 * <li>Returns a 401 Unauthorized status for invalid or missing tokens.</li>
 * </ul>
 * 
 * <p>
 * Usage:
 * </p>
 * <ul>
 * <li>This filter is automatically applied to all incoming requests as it is
 * annotated with {@link Component}.</li>
 * <li>Requests containing valid JWT tokens proceed to the next filter in the
 * chain.</li>
 * <li>Requests with invalid or missing tokens receive an HTTP 401
 * response.</li>
 * </ul>
 * 
 * <p>
 * Request Flow:
 * </p>
 * <ol>
 * <li>If the request path matches any predefined skip paths, the filter allows
 * the request to proceed without authentication.</li>
 * <li>If the Authorization header contains a valid Bearer token, it is
 * authenticated using {@link JwtAuthenticationManager}.</li>
 * <li>On successful authentication, the {@link SecurityContext} is updated, and
 * the request continues.</li>
 * <li>If authentication fails, a 401 Unauthorized response is returned.</li>
 * </ol>
 * 
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

	private final JwtAuthenticationManager jwtAuthenticationManager;

	/**
	 * Checks whether the request path should skip authentication.
	 * 
	 * @param path the request path
	 * @return {@code true} if the path should skip authentication; {@code false}
	 *         otherwise
	 */
	private boolean shouldSkip(String path) {
		if (path == null) {
			return false;
		}
		return path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/webjars")
				|| path.startsWith("/v1/api/auth");
	}

	/**
	 * Filters incoming requests for JWT authentication.
	 * 
	 * @param exchange the current server web exchange
	 * @param chain    the web filter chain
	 * @return a {@link Mono<Void>} indicating when request processing is complete
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
	    String path = exchange.getRequest().getURI().getPath();
	    if (shouldSkip(path)) {
	        return chain.filter(exchange);
	    }

	    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        return handleAuthenticationError(exchange, "No valid authorization token found");
	    }

	    String token = authHeader.substring(7);
	    return jwtAuthenticationManager.authenticate(
	            JwtAuthenticationToken.builder()
	                .token(token)
	                .build()
	        )
	        .flatMap(authentication -> {
	            authentication.setAuthenticated(true);
	            return chain.filter(exchange)
	                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
	        })
	        .onErrorResume(AuthenticationException.class, 
	            e -> handleAuthenticationError(exchange, e.getMessage()));
	}

	private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, String message) {
	    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
	    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
	    
	    ApiError error = ApiError.builder()
	        .timestamp(LocalDateTime.now().toString())
	        .status(HttpStatus.UNAUTHORIZED.value())
	        .error("Authentication Failed")
	        .message(message)
	        .path(exchange.getRequest().getPath().value())
	        .traceId(UUID.randomUUID().toString())
	        .build();

	    byte[] bytes;
	    try {
	        bytes = new ObjectMapper().writeValueAsBytes(error);
	    } catch (JsonProcessingException e) {
	        return exchange.getResponse().setComplete();
	    }

	    DataBuffer buffer = exchange.getResponse()
	        .bufferFactory()
	        .wrap(bytes);
	        
	    return exchange.getResponse().writeWith(Mono.just(buffer));
	}
}