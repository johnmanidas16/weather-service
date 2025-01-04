package com.weather.integration;

import com.weather.dto.AuthResponse;
import com.weather.dto.TokenRequest;
import com.weather.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthAPIIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void registerShouldCreateUserAndReturnToken() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser1")
                .password("password123")
                .build();

        webClient.post()
                .uri("/v1/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assertNotNull(response.getToken());
                    assertEquals("testuser1", response.getUsername());
                });
    }

    @Test
    void getTokenWithValidCredentialsShouldReturnToken() {
        TokenRequest request = TokenRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        webClient.post()
                .uri("/v1/api/auth/token")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assertNotNull(response.getToken());
                    assertEquals("testuser", response.getUsername());
                });
    }

    @Test
    void getTokenWithInvalidCredentialsShouldReturnUnauthorized() {
        TokenRequest request = TokenRequest.builder()
                .username("wronguser")
                .password("wrongpass")
                .build();

        webClient.post()
                .uri("/v1/api/auth/token")
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
