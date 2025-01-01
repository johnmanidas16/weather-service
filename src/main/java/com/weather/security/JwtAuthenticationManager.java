package com.weather.security;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * This class is responsible for authenticating users based on JSON Web Tokens (JWTs). 
 * It implements the {@link org.springframework.security.authentication.ReactiveAuthenticationManager} 
 * interface to handle authentication requests reactively.
 *
 * <p>The authentication process involves:</p>
 * <ul>
 *     <li>Receiving an {@link JwtAuthenticationToken} containing the JWT.</li>
 *     <li>Validating the JWT using the injected {@link JwtService}.</li>
 *     <li>Extracting the username from the validated token.</li>
 *     <li>Creating a new {@link JwtAuthenticationToken} with the 
 *         validated username, the original token, and a single 
 *         {@link org.springframework.security.core.authority.SimpleGrantedAuthority} 
 *         representing the "ROLE_USER" role.</li>
 * </ul>
 *
 * <p>This class is designed to be used within a Spring Security application 
 * for JWT-based authentication.</p>
 *
 */
@Component
@Primary
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
	
    private static final String ROLE_USER = "ROLE_USER";
	private final JwtService jwtService;

	/**
	 * Authenticates the provided {@link Authentication} object.
	 *
	 * <p>This method returns a {@link Mono} that emits the newly created
	 * {@link JwtAuthenticationToken} if the authentication is successful,
	 * or an empty {@link Mono} otherwise.</p>
	 *
	 * @param authentication The authentication object to authenticate.
	 * @return A {@link Mono} that emits the authenticated {@link JwtAuthenticationToken}
	 * if successful, or an empty {@link Mono} otherwise.
	 */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
            .cast(JwtAuthenticationToken.class)
            .flatMap(auth -> jwtService.validateTokenAndGetUsername(auth.getToken())
                .map(username -> {
                    List<SimpleGrantedAuthority> authorities = 
                        Collections.singletonList(new SimpleGrantedAuthority(ROLE_USER));
                    return JwtAuthenticationToken.builder()
                    		.token(auth.getToken())
                    		.username(username)
                    		.authorities(authorities)
                    		.build();
                })
            );
    }
}