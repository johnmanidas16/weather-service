package com.weather.security;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.exception.handler.GlobalExceptionHandler;

import lombok.RequiredArgsConstructor;

/**
 * This class configures Spring Security for the weather application.
 *
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	/**
     * Injected instance of the {@link JwtAuthenticationFilter} used for JWT-based authentication.
     */
	private final JwtAuthenticationFilter jwtAuthenticationFilter;


	/**
	 * Creates a {@link SecurityWebFilterChain} bean to configure Spring Security.
	 *
	 * <p>This method defines the security configuration for the application. Here's a breakdown
	 * of the configuration steps:</p>
	 *
	 * <ul>
	 *     <li>Disables CSRF protection as it might not be necessary for weather API endpoints.</li>
	 *     <li>Defines authorization rules:
	 *         <ul>
	 *             <li>Grants public access to paths related to API documentation (Swagger UI)
	 *                 and authentication (/v1/api/auth/**).</li>
	 *             <li>Requires authentication for requests to "/api/weather/**" endpoints.</li>
	 *             <li>Requires authentication for any other unmatched requests.</li>
	 *         </ul>
	 *     </li>
	 *     <li>Disables HTTP Basic and form login authentication as JWT is the chosen method.</li>
	 *     <li>Adds the {@link JwtAuthenticationFilter} to the filter chain at {@link SecurityWebFiltersOrder#AUTHENTICATION}
	 *         order to perform JWT-based authentication before other security filters.</li>
	 * </ul>
	 *
	 * @param http The {@link ServerHttpSecurity} object used to configure security.
	 * @return A {@link SecurityWebFilterChain} bean containing the security configuration.
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	    return http
	        .csrf(csrf -> csrf.disable())
	        .authorizeExchange(exchanges -> exchanges
	            .pathMatchers(
	                "/v3/api-docs/**",
	                "/webjars/**", 
	                "/swagger-ui/**",
	                "/v1/api/auth/register", "/v1/api/auth/token"
	            ).permitAll()
	            .anyExchange().authenticated()
	        )
	        .httpBasic(httpBasic -> httpBasic.disable())
	        .formLogin(formLogin -> formLogin.disable())
	        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
	        .build();
	}
	
	@Bean
	public ErrorWebExceptionHandler errorWebExceptionHandler(ObjectMapper objectMapper) {
	    return new GlobalExceptionHandler(objectMapper);
	}
}