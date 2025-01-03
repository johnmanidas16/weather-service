package com.weather.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weather.dto.AuthResponse;
import com.weather.dto.TokenRequest;
import com.weather.dto.UserRegistrationRequest;
import com.weather.security.JwtService;
import com.weather.service.impl.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST Controller for authentication and user management APIs. Provides
 * endpoints for user registration, token generation, and user
 * activation/deactivation.
 */
@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management operations")
public class AuthAPI {

	private final UserService userService;
	private final JwtService jwtService;

	/**
	 * Registers a new user in the system.
	 *
	 * @param request {@link UserRegistrationRequest} containing user registration
	 *                details.
	 * @return A {@link Mono} emitting {@link AuthResponse} containing the generated
	 *         token and username.
	 */
	@Operation(summary = "Register a new user", description = "Registers a new user and generates a JWT token.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User registered successfully", 
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content) })
	@PostMapping("/register")
	public Mono<AuthResponse> register(@RequestBody UserRegistrationRequest request) {
		return userService.createUser(request)
				.map(user -> AuthResponse.builder()
				.token(jwtService.generateToken(user.getUsername()))
				.username(user.getUsername())
				.build());
	}

	/**
	 * Generates a token for an authenticated user.
	 *
	 * @param request {@link TokenRequest} containing the username and password for
	 *                authentication.
	 * @return A {@link Mono} emitting {@link AuthResponse} containing the generated
	 *         token and username.
	 */
	@Operation(summary = "Generate JWT token", description = "Authenticates the user and generates a JWT token.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Token generated successfully", 
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid credentials", content = @Content),
			@ApiResponse(responseCode = "403", description = "Authentication failed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content) })
	@PostMapping("/token")
	public Mono<AuthResponse> getToken(@RequestBody TokenRequest request) {
		return userService.authenticate(request.getUsername(), request.getPassword())
				.map(user -> AuthResponse.builder()
				.token(jwtService.generateToken(user.getUsername()))
				.username(user.getUsername())
				.build());
	}
}
