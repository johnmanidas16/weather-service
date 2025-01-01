package com.weather.webclient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.io.IOException;

class WebClientServiceTest {
    private WebClientService webClientService;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        webClientService = new WebClientService();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void executeRequestSuccessfulResponseReturnsDataTest() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"data\": \"test\"}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(webClientService.executeRequest(
                        mockWebServer.url("/").toString(),
                        "/test",
                        "trace-id",
                        HttpMethod.GET,
                        String.class
                ))
                .expectNext("{\"data\": \"test\"}")
                .verifyComplete();
    }

    @Test
    void executeRequestNotFoundResponseFailsWithoutRetryTest() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(webClientService.executeRequest(
                        mockWebServer.url("/").toString(),
                        "/test",
                        "trace-id",
                        HttpMethod.GET,
                        String.class
                ))
                .expectError(WebClientResponseException.class)
                .verify();
    }
}
