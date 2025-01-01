package com.weather.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private static final String TEST_SECRET = "dK3yL8xR#mP9$vN2cF5jH1qW4tY7*zE6aB0nM3uQ8sW4pX2vB9yN5mC7kD1fG3hJ5nM8tP4rL6wS9xF2aE4bV7";
    private static final String TEST_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
    }

    @Test
    void generateTokenValidUsernameReturnsValidTokenTest() {
        String token = jwtService.generateToken(TEST_USERNAME);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void validateTokenAndGetUsernameValidTokenReturnsUsernameTest() {
        String token = jwtService.generateToken(TEST_USERNAME);

        StepVerifier.create(jwtService.validateTokenAndGetUsername(token))
                .expectNext(TEST_USERNAME)
                .verifyComplete();
    }

    @Test
    void validateTokenAndGetUsernameInvalidTokenReturnsErrorTest() {
        String invalidToken = "invalid.token.here";

        StepVerifier.create(jwtService.validateTokenAndGetUsername(invalidToken))
                .expectError()
                .verify();
    }
}

