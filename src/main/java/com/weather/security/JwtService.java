package com.weather.security;

import static com.weather.utils.WeatherServiceConst.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.weather.exception.InvalidTokenException;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * This class provides services for generating and validating JSON Web Tokens (JWTs) 
 * used for authentication in the weather application.
 *
 */
@Service
@RequiredArgsConstructor
public class JwtService {

	/**
     * The secret key used for signing and verifying JWTs. This value is injected 
     * from the application properties using Spring's `@Value` annotation.
     */
	@Value("${jwt.secret}")
	private String secret;

	/**
	 * Generates a JWT token for the given username.
	 *
	 * <p>This method creates a JWT token with the following claims:</p>
	 * <ul>
	 *     <li><b>typ</b>: Set to "BEARER" indicating the token type.</li>
	 *     <li><b>roles</b>: A list containing a single role "ROLE_USER".</li>
	 *     <li><b>iss</b>: Set to "WEATHER_SERVICE" identifying the issuer of the token.</li>
	 *     <li><b>aud</b>: Set to "WEATHER_API" specifying the intended audience for the token.</li>
	 *     <li><b>iat</b>: The issued at time of the token.</li>
	 *     <li><b>exp</b>: The expiration time of the token, set to 10 hours from the issued at time.</li>
	 *     <li><b>sub</b>: The username for which the token is generated.</li>
	 *     <li><b>jti</b>: A unique identifier for the token (JWT ID).</li>
	 * </ul>
	 *
	 * <p>The token is signed using the HMAC SHA-512 algorithm with the secret key.</p>
	 *
	 * @param username The username for which to generate the token.
	 * @return The generated JWT token as a String.
	 */
	public String generateToken(String username) {
	    Map<String, Object> claims = new HashMap<>();
	    claims.put(TYPE, BEARER);
	    claims.put(ROLES, Collections.singletonList(ROLE_USER));
	    claims.put(ISSUER, WEATHER_SERVICE);
	    claims.put(AUDIENCE, WEATHER_API);
	    claims.put(CREATED, new Date());

	    return Jwts.builder()
	        .setClaims(claims)
	        .setSubject(username)
	        .setId(UUID.randomUUID().toString())
	        .setIssuedAt(new Date())
	        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
	        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
	        .compact();
	}

	/**
	 * Validates the given JWT token and extracts the username from the claims.
	 *
	 * <p>This method attempts to parse the JWT token using the configured secret key.
	 * If the token is valid, it extracts the username from the subject claim.
	 * Otherwise, it throws an {@link InvalidTokenException}.</p>
	 *
	 * <p>The returned {@link Mono} emits the username if the token is valid,
	 * or emits an error containing an {@link InvalidTokenException} if the token is invalid.</p>
	 *
	 * @param token The JWT token to validate.
	 * @return A {@link Mono} that emits the username if the token is valid,
	 * or an error containing an {@link InvalidTokenException} otherwise.
	 */
	public Mono<String> validateTokenAndGetUsername(String token) {
		try {
			String username = Jwts.parserBuilder()
					.setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build()
					.parseClaimsJws(token).getBody().getSubject();
			return Mono.just(username);
		} catch (JwtException e) {
			return Mono.error(new InvalidTokenException("Invalid JWT token"));
		}
	}
}