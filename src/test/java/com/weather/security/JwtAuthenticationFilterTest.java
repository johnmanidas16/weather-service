package com.weather.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtAuthenticationManager authManager;

    @Mock
    private WebFilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter authFilter;

    private static final String TEST_TOKEN = "test.token.here";

    @Test
    void filterValidTokenAuthenticatesTest() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/weather/data")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
                        .build()
        );

        JwtAuthenticationToken authToken = createAuthToken(true);
        when(authManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(authToken));
        when(filterChain.filter(exchange))
                .thenReturn(Mono.empty());

        StepVerifier.create(authFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    @Test
    void filterNoTokenReturnsUnauthorizedTest() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/weather/data")
                        .build()
        );

        StepVerifier.create(authFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    @Test
    void filterPublicPathSkipsAuthenticationTest() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/api/auth/login")
                        .build()
        );

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(authFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    private JwtAuthenticationToken createAuthToken(boolean authenticated) {
        JwtAuthenticationToken token = JwtAuthenticationToken.builder()
                .token(TEST_TOKEN)
                .username("testUser")
                .build();
        token.setAuthenticated(authenticated);
        return token;
    }
}
