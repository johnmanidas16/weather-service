package com.weather.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationManagerTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationManager authManager;

    private static final String TEST_TOKEN = "test.token.here";
    private static final String TEST_USERNAME = "testUser";

    @Test
    void authenticateValidTokenReturnsAuthenticatedTokenTest() {
        JwtAuthenticationToken token = JwtAuthenticationToken.builder()
                .token(TEST_TOKEN)
                .build();

        when(jwtService.validateTokenAndGetUsername(TEST_TOKEN))
                .thenReturn(Mono.just(TEST_USERNAME));

        StepVerifier.create(authManager.authenticate(token))
                .expectNextMatches(auth -> {
                    assertAuthenticationToken(auth);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void authenticateInvalidTokenReturnsEmptyTest() {
        JwtAuthenticationToken token = JwtAuthenticationToken.builder()
                .token(TEST_TOKEN)
                .build();

        StepVerifier.create(authManager.authenticate(token))
                .expectError();
    }

    private void assertAuthenticationToken(Authentication auth) {
        assertTrue(auth instanceof JwtAuthenticationToken);
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        assertEquals(TEST_USERNAME, jwtAuth.getUsername());
        assertEquals(TEST_TOKEN, jwtAuth.getToken());
        assertEquals(1, jwtAuth.getAuthorities().size());
        assertTrue(jwtAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
