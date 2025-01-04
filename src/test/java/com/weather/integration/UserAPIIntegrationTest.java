package com.weather.integration;

import com.weather.dto.AuthResponse;
import com.weather.dto.TokenRequest;
import com.weather.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserAPIIntegrationTest {
    @Autowired
    private WebTestClient webClient;

    private String authToken;

    @BeforeEach
    void setUp() {
        TokenRequest request = TokenRequest.builder()
                .username("testuser1")
                .password("password123")
                .build();

        authToken = webClient.post()
                .uri("/v1/api/auth/token")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .returnResult(AuthResponse.class)
                .getResponseBody()
                .blockFirst()
                .getToken();
    }

    @Test
    void activateUserWithValidUserShouldActivateAndReturnUser() {
        webClient.put()
                .uri("/v1/api/user/users/{username}/activate", "testuser1")
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assertNotNull(user);
                    assertEquals("testuser1", user.getUsername());
                    assertTrue(user.isActive());
                });
    }

    @Test
    void activateUserWithNonexistentUserShouldReturn404() {
        webClient.put()
                .uri("/v1/api/user/users/{username}/activate", "nonexistentuser")
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deactivateUserWithValidUserShouldDeactivateAndReturnUser() {
        webClient.put()
                .uri("/v1/api/user/users/{username}/deactivate", "testuser1")
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assertNotNull(user);
                    assertEquals("testuser1", user.getUsername());
                    assertFalse(user.isActive());
                });
    }

    @Test
    void deactivateUserWithNonexistentUserShouldReturn404() {
        webClient.put()
                .uri("/v1/api/user/users/{username}/deactivate", "nonexistentuser")
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void activateUserWithoutAuthShouldReturn401() {
        webClient.put()
                .uri("/v1/api/user/users/{username}/activate", "testuser")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
