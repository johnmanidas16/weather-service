package com.weather.service;

import com.mongodb.MongoException;
import com.weather.dto.Coordinates;
import com.weather.dto.WeatherRequest;
import com.weather.exception.DatabaseException;
import com.weather.model.WeatherData;
import com.weather.repository.WeatherDataRepository;
import com.weather.service.impl.WeatherServiceImpl;
import com.weather.utils.WeatherServiceProperties;
import com.weather.utils.WeatherServiceUriUtil;
import com.weather.webclient.WebClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {
    public static final String GEO_1_0_ZIP = "/geo/1.0/zip";
    public static final String DATA_2_5_WEATHER = "/data/2.5/weather";

    @Mock
    private WebClientService webClientService;

    @Mock
    private WeatherServiceProperties weatherServiceProperties;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private WeatherServiceUriUtil weatherServiceUriUtil;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private WeatherRequest testRequest;
    private WeatherData testWeatherData;
    private Coordinates testCoordinates;

    @BeforeEach
    void setUp() {
        testCoordinates = Coordinates.builder()
                .lat(40)
                .lon(-74)
                .country("US")
                .build();

        testRequest = WeatherRequest.builder()
                .postalCode("12345")
                .username("testUser")
                .build();

        testWeatherData = createTestWeatherData();
    }

    @Test
    void getWeatherDataValidRequestSuccessTest() {
        String baseUrl = "http://test-url";
        String geoUri = "/geo/1.0/zip";
        String weatherUri = "/data/2.5/weather";

        lenient().when(weatherServiceProperties.getUrl()).thenReturn(baseUrl);
        lenient().when(weatherServiceUriUtil.prepareGeoCoordinatesUri(testRequest.getPostalCode())).thenReturn(geoUri);
        lenient().when(weatherServiceUriUtil.prepareWeatherDataUri(any(Coordinates.class))).thenReturn(weatherUri);

        lenient().when(webClientService.executeRequest(
                eq(baseUrl),
                eq(geoUri),
                eq(""),
                eq(HttpMethod.GET),
                eq(Coordinates.class)
        )).thenReturn(Mono.just(testCoordinates));

        lenient().when(webClientService.executeRequest(
                eq(baseUrl),
                eq(weatherUri),
                eq(""),
                eq(HttpMethod.GET),
                eq(WeatherData.class)
        )).thenReturn(Mono.just(testWeatherData));

        lenient().when(weatherDataRepository.save(any(WeatherData.class)))
                .thenReturn(Mono.just(testWeatherData));

        StepVerifier.create(weatherService.getWeatherData(testRequest))
                .assertNext(weatherData -> {
                    assertNotNull(weatherData);
                    assertEquals(testRequest.getPostalCode(), weatherData.getPostalCode());
                    assertEquals(testRequest.getUsername(), weatherData.getUsername());
                    assertNotNull(weatherData.getRequestTime());
                })
                .verifyComplete();
    }

    @Test
    void getWeatherDataDatabaseErrorThrowsDatabaseExceptionTest() {
        when(weatherServiceProperties.getUrl()).thenReturn("http://test-url");

        String geoUri = GEO_1_0_ZIP;
        String weatherUri = DATA_2_5_WEATHER;
        when(weatherServiceUriUtil.prepareGeoCoordinatesUri(testRequest.getPostalCode())).thenReturn(geoUri);
        when(weatherServiceUriUtil.prepareWeatherDataUri(any(Coordinates.class))).thenReturn(weatherUri);

        when(webClientService.executeRequest(
                eq("http://test-url"),
                eq(geoUri),
                eq(""),
                eq(HttpMethod.GET),
                eq(Coordinates.class)
        )).thenReturn(Mono.just(testCoordinates));

        when(webClientService.executeRequest(
                eq("http://test-url"),
                eq(weatherUri),
                eq(""),
                eq(HttpMethod.GET),
                eq(WeatherData.class)
        )).thenReturn(Mono.just(testWeatherData));


        when(weatherDataRepository.save(any(WeatherData.class)))
                .thenReturn(Mono.error(new MongoException("Database Error")));


        StepVerifier.create(weatherService.getWeatherData(testRequest))
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void getHistoryByPostalCodeSuccessTest() {
        when(weatherDataRepository.findByPostalCodeOrderByRequestTimeDesc(anyString()))
                .thenReturn(Flux.just(testWeatherData));

        StepVerifier.create(weatherService.getHistoryByPostalCode("12345"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("12345", response.getPostalCode());
                    assertNotNull(response.getCurrent());
                    assertEquals(1, response.getHistory().size());
                })
                .verifyComplete();
    }

    @Test
    void getHistoryByUsernameSuccessTest() {
        when(weatherDataRepository.findByUsernameOrderByRequestTimeDesc(anyString()))
                .thenReturn(Flux.just(testWeatherData));

        StepVerifier.create(weatherService.getHistoryByUsername("testUser"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("testUser", response.getUsername());
                    assertNotNull(response.getCurrent());
                    assertEquals(1, response.getHistory().size());
                })
                .verifyComplete();
    }

    private WeatherData createTestWeatherData() {
        WeatherData.Weather weather = new WeatherData.Weather(800, "Clear", "clear sky", "01d");
        WeatherData.Main main = new WeatherData.Main(72.5, 70.0, 68.0, 75.0, 1013, 65, 1015, 1012);
        WeatherData.Wind wind = new WeatherData.Wind(5.5, 180);
        WeatherData.Clouds clouds = new WeatherData.Clouds(0);
        WeatherData.Sys sys = new WeatherData.Sys(1, 123, "US", 1622520000L, 1622570000L);
        WeatherData.Coord coord = new WeatherData.Coord(-74.0060, 40.7128);

        return WeatherData.builder()
                .uuid("test-id")
                .coord(coord)
                .weather(Collections.singletonList(weather))
                .base("stations")
                .main(main)
                .visibility(10000)
                .wind(wind)
                .clouds(clouds)
                .dt(1622550000L)
                .sys(sys)
                .timezone(-14400)
                .name("New York")
                .cod(200)
                .postalCode("12345")
                .username("testUser")
                .requestTime(LocalDateTime.now())
                .build();
    }
}
