package com.weather.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weather.dto.TokenRequest;
import com.weather.dto.UserRegistrationRequest;
import com.weather.model.User;
import com.weather.security.JwtService;
import com.weather.service.impl.UserService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AuthAPITest {

	@Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthAPI authAPI;

    private User testUser;
    private String testToken;
    private UserRegistrationRequest registrationRequest;
    private TokenRequest tokenRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testUser")
                .password("password123")
                .active(true)
                .build();

        testToken = "test.jwt.token";

        registrationRequest = UserRegistrationRequest.builder()
                .username("testUser")
                .password("password123")
                .build();

        tokenRequest = TokenRequest.builder()
                .username("testUser")
                .password("password123")
                .build();
    }

    @Test
    void registerValidRequestReturnsAuthResponseTest() {
        when(userService.createUser(any(UserRegistrationRequest.class)))
                .thenReturn(Mono.just(testUser));
        when(jwtService.generateToken(anyString()))
                .thenReturn(testToken);

        StepVerifier.create(authAPI.register(registrationRequest))
                .assertNext(response -> {
                    assertEquals(testUser.getUsername(), response.getUsername());
                    assertEquals(testToken, response.getToken());
                })
                .verifyComplete();
    }

    @Test
    void getTokenValidCredentialsReturnsAuthResponseTest() {
        when(userService.authenticate(anyString(), anyString()))
                .thenReturn(Mono.just(testUser));
        when(jwtService.generateToken(anyString()))
                .thenReturn(testToken);

        StepVerifier.create(authAPI.getToken(tokenRequest))
                .assertNext(response -> {
                    assertEquals(testUser.getUsername(), response.getUsername());
                    assertEquals(testToken, response.getToken());
                })
                .verifyComplete();
    }

    @Test
    void authenticateWhenUserServiceFailsReturnsErrorTest() {
        when(userService.authenticate(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Authentication failed")));

        StepVerifier.create(authAPI.getToken(tokenRequest))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void registerWhenUserServiceFailsReturnsErrorTest() {
        when(userService.createUser(any(UserRegistrationRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Registration failed")));

        StepVerifier.create(authAPI.register(registrationRequest))
                .expectError(RuntimeException.class)
                .verify();
    }
}