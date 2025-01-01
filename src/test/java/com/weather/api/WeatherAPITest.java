package com.weather.api;

import com.weather.dto.WeatherInfo;
import com.weather.dto.WeatherRequest;
import com.weather.dto.WeatherResponse;
import com.weather.service.impl.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherAPITest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherAPI weatherAPI;

    private WeatherRequest testWeatherRequest;
    private WeatherResponse testWeatherResponse;
    private WeatherInfo testWeatherInfo;

    @BeforeEach
    void setUp() {
        testWeatherInfo = WeatherInfo.builder()
                .timestamp(LocalDateTime.now())
                .temperature(24)
                .feelsLike(24)
                .humidity(23)
                .description("Sunny")
                .windSpeed(34)
                .conditions("Clear")
                .username("testUser")
                .postalCode("12345")
                .build();

        testWeatherRequest = WeatherRequest.builder()
                .postalCode("12345")
                .username("testUser")
                .build();

        testWeatherResponse = WeatherResponse.builder()
                .postalCode("12345")
                .timestamp(LocalDateTime.now())
                .current(testWeatherInfo)
                .history(Arrays.asList(testWeatherInfo))
                .build();
    }

    @Test
    void getHistoryByPostalCodeValidPostalCodeReturnsWeatherResponseTest() {
        when(weatherService.getHistoryByPostalCode(anyString()))
                .thenReturn(Mono.just(testWeatherResponse));

        StepVerifier.create(weatherAPI.getHistoryByPostalCode("12345"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(200, response.getStatusCode().value());
                    WeatherResponse responseBody = response.getBody();
                    assertNotNull(responseBody);
                    assertEquals(1, responseBody.getHistory().size());
                    assertEquals(testWeatherResponse.getPostalCode(), responseBody.getPostalCode());
                    assertEquals(testWeatherInfo.getPostalCode(),
                            responseBody.getHistory().get(0).getPostalCode());
                })
                .verifyComplete();
    }

    @Test
    void getHistoryByUsernameValidUsernameReturnsWeatherResponseTest() {
        when(weatherService.getHistoryByUsername(anyString()))
                .thenReturn(Mono.just(testWeatherResponse));

        StepVerifier.create(weatherAPI.getHistoryByUsername("testUser"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(200, response.getStatusCode().value());
                    WeatherResponse responseBody = response.getBody();
                    assertNotNull(responseBody);
                    assertEquals(1, responseBody.getHistory().size());
                    assertEquals(testWeatherInfo.getUsername(),
                            responseBody.getHistory().get(0).getUsername());
                })
                .verifyComplete();
    }

    @Test
    void collectEventServiceErrorReturnsErrorTest() {
        when(weatherService.getWeatherData(any(WeatherRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        StepVerifier.create(weatherAPI.collectEvent(testWeatherRequest))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getHistoryByPostalCodeServiceErrorReturnsErrorTest() {
        when(weatherService.getHistoryByPostalCode(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        StepVerifier.create(weatherAPI.getHistoryByPostalCode("12345"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getHistoryByUsernameServiceErrorReturnsErrorTest() {
        when(weatherService.getHistoryByUsername(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        StepVerifier.create(weatherAPI.getHistoryByUsername("testUser"))
                .expectError(RuntimeException.class)
                .verify();
    }
}