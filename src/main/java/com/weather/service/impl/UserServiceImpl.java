package com.weather.service.impl;

import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.weather.dto.UserRegistrationRequest;
import com.weather.exception.InvalidCredentialsException;
import com.weather.exception.UserAlreadyExistsException;
import com.weather.exception.UserNotFoundException;
import com.weather.model.User;
import com.weather.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Implementation of the {@link UserService} interface.
 * Provides services for user registration, authentication, and account management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	public static final String ROLE_USER = "ROLE_USER";
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
     * Registers a new user with the given details.
     *
     * @param request {@link UserRegistrationRequest} containing user details such as username, password, and postal code.
     * @return A {@link Mono} emitting the saved {@link User} upon successful registration.
     * @throws UserAlreadyExistsException if a user with the same username already exists.
     */
	@Override
	public Mono<User> createUser(UserRegistrationRequest request) {
		return userRepository.findByUsername(request.getUsername())
				.flatMap(existingUser -> Mono.<User>error(
						new UserAlreadyExistsException("Username already exists: " + request.getUsername())))
				.switchIfEmpty(Mono.defer(() -> {
					User newUser = new User();
					newUser.setUsername(request.getUsername());
					newUser.setPassword(passwordEncoder.encode(request.getPassword()));
					newUser.setPostalCode(request.getPostalCode());
					newUser.setActive(true);
					newUser.setRoles(Collections.singletonList(ROLE_USER));

					return userRepository.save(newUser);
				})).doOnSuccess(user -> log.info("Created new user: {}", user.getUsername()))
				.doOnError(error -> log.error("Error creating user: {}", error.getMessage()));
	}

	/**
     * Authenticates a user by verifying their username and password.
     *
     * @param username The username of the user.
     * @param password The plain-text password to be verified.
     * @return A {@link Mono} emitting the authenticated {@link User} if credentials are valid.
     * @throws InvalidCredentialsException if the username or password is incorrect.
     */
	@Override
	public Mono<User> authenticate(String username, String password) {
		return userRepository.findByUsername(username)
	            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
	            .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid username or password")));
	}
	
	/**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return A {@link Mono} emitting the {@link User} if found.
     * @throws UserNotFoundException if no user with the given username exists.
     */
	@Override
	public Mono<User> findByUsername(String username) {
		return userRepository.findByUsername(username)
				.switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + username)));
	}

	/**
     * Activates a user account by setting the active flag to true.
     *
     * @param username The username of the user to activate.
     * @return A {@link Mono} emitting the updated {@link User} upon successful activation.
     * @throws UserNotFoundException if no user with the given username exists.
     */
	@Override
	public Mono<User> activateUser(String username) {
		return userRepository.findByUsername(username)
				.switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + username))).flatMap(user -> {
					user.setActive(true);
					return userRepository.save(user);
				}).doOnSuccess(user -> log.info("Activated user: {}", username));
	}

	/**
     * Deactivates a user account by setting the active flag to false.
     *
     * @param username The username of the user to deactivate.
     * @return A {@link Mono} emitting the updated {@link User} upon successful deactivation.
     * @throws UserNotFoundException if no user with the given username exists.
     */
	@Override
	public Mono<User> deactivateUser(String username) {
		return userRepository.findByUsername(username)
				.switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + username))).flatMap(user -> {
					user.setActive(false);
					return userRepository.save(user);
				}).doOnSuccess(user -> log.info("Deactivated user: {}", username));
	}
}
