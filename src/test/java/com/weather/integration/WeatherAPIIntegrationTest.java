package com.weather.integration;

import com.weather.dto.AuthResponse;
import com.weather.dto.TokenRequest;
import com.weather.dto.WeatherRequest;
import com.weather.dto.WeatherResponse;
import com.weather.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeatherAPIIntegrationTest {
    private static final String POSTAL_CODE = "73001";
    private static final String POSTAL_CODE_INVALID = "121";
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
    void collectEventWithValidRequestShouldReturnWeatherData() {
        WeatherRequest request = WeatherRequest.builder()
                .postalCode(POSTAL_CODE)
                .username("testuser1")
                .build();

        webClient.post()
                .uri("/v1/api/weather/info")
                .headers(headers -> headers.setBearerAuth(authToken))
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WeatherData.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(POSTAL_CODE, response.getPostalCode());
                });
    }

    @Test
    void getHistoryByPostalCodeWithValidPostalCodeShouldReturnWeatherHistory() {
        String postalCode = POSTAL_CODE;

        webClient.get()
                .uri("/v1/api/weather/history/postal-code/{postalCode}", postalCode)
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(WeatherResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertTrue(response.getCurrent()!= null);
                });
    }

    @Test
    void getHistoryByUsernameWithValidUsernameShouldReturnWeatherHistory() {
        String username = "testuser";

        webClient.get()
                .uri("/v1/api/weather/history/user/{username}", username)
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(WeatherResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertFalse(response.getCurrent()!= null);
                });
    }

    @Test
    void collectEventWithInvalidRequestShouldReturn400() {
        WeatherRequest request = WeatherRequest.builder()
                .postalCode(POSTAL_CODE)
                .build();

        webClient.post()
                .uri("/v1/api/weather/info")
                .headers(headers -> headers.setBearerAuth(authToken))
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void anyEndpointWithoutAuthShouldReturn401() {
        webClient.get()
                .uri("/v1/api/weather/history/postal-code/73001")
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
