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

import static com.weather.utils.ErrorConstants.*;

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
						new UserAlreadyExistsException(USERNAME_ALREADY_EXISTS + request.getUsername())))
				.switchIfEmpty(Mono.defer(() -> {
					User newUser = getUserDetails(request);
					return userRepository.save(newUser);
				})).doOnSuccess(user -> log.info("Created new user: {}", user.getUsername()))
				.doOnError(error -> log.error("Error creating user: {}", error.getMessage()));
	}

	/**
	 * Creates a new User entity from the registration request data.
	 * The password is encoded using the configured password encoder,
	 * and the user is set as active with default USER role.
	 *
	 * @param request The {@link UserRegistrationRequest} containing user registration details
	 * @return {@link User} entity with encoded password and default settings
	 */
	private User getUserDetails(UserRegistrationRequest request) {
		User newUser = new User();
		newUser.setUsername(request.getUsername());
		newUser.setPassword(passwordEncoder.encode(request.getPassword()));
		newUser.setPostalCode(request.getPostalCode());
		newUser.setActive(true);
		newUser.setRoles(Collections.singletonList(ROLE_USER));
		return newUser;
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
	            .switchIfEmpty(Mono.error(new InvalidCredentialsException(INVALID_USERNAME_OR_PASSWORD)));
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
				.switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND + username)));
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
				.switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND + username))).flatMap(user -> {
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
				.switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND + username))).flatMap(user -> {
					user.setActive(false);
					return userRepository.save(user);
				}).doOnSuccess(user -> log.info("Deactivated user: {}", username));
	}
}
