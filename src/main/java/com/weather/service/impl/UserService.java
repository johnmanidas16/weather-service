package com.weather.service.impl;

import com.weather.dto.UserRegistrationRequest;
import com.weather.model.User;

import reactor.core.publisher.Mono;

public interface UserService {

	/**
	 * 
	 * @param request
	 * @return
	 */
	Mono<User> createUser(UserRegistrationRequest request);

	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	Mono<User> authenticate(String username, String password);
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<User> findByUsername(String username);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<User> activateUser(String username);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<User> deactivateUser(String username);
}
