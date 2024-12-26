package com.weather.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weather.dto.WeatherRequest;
import com.weather.dto.WeatherResponse;
import com.weather.model.WeatherData;
import com.weather.service.impl.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for weather-related APIs.
 * Provides endpoints for collecting weather data and retrieving historical weather information.
 */
@Slf4j
@RestController
@RequestMapping("/v1/api/weather")
@RequiredArgsConstructor
public class WeatherAPI {
	
	private final WeatherService weatherService;

	/**
     * Collects weather data based on the provided request.
     *
     * @param weatherRequest {@link WeatherRequest} containing details about the weather to be collected.
     * @return A {@link Mono} emitting a {@link ResponseEntity} with the collected {@link WeatherData}.
     */
    @Operation(summary = "Collect weather data", description = "Collects weather data for the given parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weather data collected successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherData.class))),
            @ApiResponse(responseCode = "404", description = "No weather data found for the given postal code", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
	@PostMapping("/info")
	public Mono<ResponseEntity<WeatherData>> collectEvent(@Valid @RequestBody WeatherRequest weatherRequest) {
	
		log.debug("Raw request body: {}", weatherRequest);
		return weatherService.getWeatherData(weatherRequest).map(ResponseEntity::ok);
	}

    /**
     * Retrieves historical weather data by postal code.
     *
     * @param postalCode The postal code for which historical weather data is retrieved.
     * @return A {@link Flux} emitting {@link ResponseEntity} containing the historical {@link WeatherData}.
     */
    @Operation(summary = "Retrieve weather history by postal code", 
               description = "Fetches historical weather data for the given postal code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weather history retrieved successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherData.class))),
            @ApiResponse(responseCode = "404", description = "No weather data found for the given postal code", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
	@GetMapping("/history/postal-code/{postalCode}")
	public Mono<ResponseEntity<WeatherResponse>> getHistoryByPostalCode(@PathVariable String postalCode) {
		return weatherService.getHistoryByPostalCode(postalCode).map(ResponseEntity::ok);
	}

    /**
     * Retrieves historical weather data by username.
     *
     * @param username The username for which historical weather data is retrieved.
     * @return A {@link Flux} emitting {@link ResponseEntity} containing the historical {@link WeatherData}.
     */
    @Operation(summary = "Retrieve weather history by username", 
               description = "Fetches historical weather data for the given username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weather history retrieved successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherData.class))),
            @ApiResponse(responseCode = "404", description = "No weather data found for the given username", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
	@GetMapping("/history/user/{username}")
	public Mono<ResponseEntity<WeatherResponse>> getHistoryByUsername(@PathVariable String username) {
		return weatherService.getHistoryByUsername(username).map(ResponseEntity::ok);
	}
}
