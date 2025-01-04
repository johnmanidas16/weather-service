package com.weather.service.impl;



import java.time.LocalDateTime;
import java.util.List;

import com.weather.exception.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mongodb.MongoException;
import com.weather.dto.Coordinates;
import com.weather.dto.WeatherInfo;
import com.weather.dto.WeatherRequest;
import com.weather.dto.WeatherResponse;
import com.weather.model.WeatherData;
import com.weather.repository.WeatherDataRepository;
import com.weather.utils.WeatherServiceProperties;
import com.weather.utils.WeatherServiceUriUtil;
import com.weather.webclient.WebClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static com.weather.utils.ErrorConstants.*;

/**
 * Implementation of the {@link WeatherService} interface.
 * Provides weather data retrieval and history services.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WeatherServiceImpl implements WeatherService {

	public static final String REGEX = "^\\d{5}$";
	private final WeatherServiceProperties weatherServiceProperties;
	private final WebClientService webClientService;
	private final WeatherDataRepository weatherDataRepository;
	private final WeatherServiceUriUtil weatherServiceUriUtil;

	/**
	 * Fetches weather data based on the given request.
	 *
	 * @param request The {@link WeatherRequest} containing the postal code and username.
	 * @return A {@link Mono} emitting the saved {@link WeatherData}.
	 * @throws ValidationException     If the request or postal code is invalid.
	 * @throws WeatherServiceException If fetching weather data from the external service fails.
	 * @throws DatabaseException       If saving data to the database fails.
	 */
	public Mono<WeatherData> getWeatherData(WeatherRequest request) {
		return validateUserAccess(request)
				.then(validateRequest(request))
				.then(getCoordinates(request))
				.flatMap(this::getWeatherDetails)
				.map(weatherData -> {
					mapMetaData(request, weatherData);
					return weatherData;
				}).flatMap(weatherDataRepository::save)
				.onErrorMap(WebClientResponseException.class,
						ex -> new WeatherServiceException("Failed to fetch weather data: " + ex.getMessage(), ex))
				.onErrorMap(MongoException.class,
						ex -> new DatabaseException(DATABASE_ERROR_WHILE_SAVING_WEATHER_DATA, ex))
				.doOnError(ex -> log.error("Error processing weather request: {}", ex.getMessage()));
	}

	/**
	 * Validates that the user making the request is accessing their own weather data.
	 * (obtained from the security context) with the username provided in the weather request.
	 * This ensures that users can only access their own weather data, implementing
	 *
	 * @param request The {@link WeatherRequest} containing metadata information.
	 *                throws an {@link UnauthorizedAccessException} if they don't match
	 */
	private Mono<Void> validateUserAccess(WeatherRequest request) {
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getName)
				.flatMap(tokenUsername -> {
					if (!tokenUsername.equals(request.getUsername())) {
						return Mono.error(new UnauthorizedAccessException(
								ACCESS_DENIED_YOU_CAN_ONLY_ACCESS_YOUR_OWN_WEATHER_DATA
						));
					}
					return Mono.empty();
				});
	}


	/**
	 * Maps metadata such as postal code, username, and request time to the weather data.
	 *
	 * @param request     The {@link WeatherRequest} containing metadata information.
	 * @param weatherData The {@link WeatherData} to be updated.
	 */
	private void mapMetaData(WeatherRequest request, WeatherData weatherData) {
		weatherData.setPostalCode(request.getPostalCode());
		weatherData.setUsername(request.getUsername());
		weatherData.setRequestTime(LocalDateTime.now());
	}

	/**
	 * Validates the incoming {@link WeatherRequest}.
	 *
	 * @param request The request to validate.
	 * @return A {@link Mono<Void>} indicating completion.
	 * @throws ValidationException If the request is invalid or the postal code format is incorrect.
	 */
	private Mono<Void> validateRequest(WeatherRequest request) {
		return Mono.just(request).filter(req -> req != null && req.getPostalCode() != null)
				.switchIfEmpty(Mono.error(new ValidationException(INVALID_REQUEST_MISSING_POSTAL_CODE)))
				.filter(req -> req.getPostalCode()
						.matches(REGEX))
				.switchIfEmpty(Mono.error(new ValidationException(INVALID_POSTAL_CODE_FORMAT)))
				.then();
	}

	/**
	 * Retrieves geographic coordinates based on the postal code in the request.
	 *
	 * @param request The {@link WeatherRequest} containing the postal code.
	 * @return A {@link Mono<Coordinates>} with the geographic coordinates.
	 * @throws ResourceNotFoundException If the postal code is not found.
	 * @throws WeatherServiceException   If an error occurs while fetching coordinates.
	 */
	private Mono<Coordinates> getCoordinates(WeatherRequest request) {
		return webClientService.executeRequest(weatherServiceProperties.getUrl(),
				weatherServiceUriUtil.prepareGeoCoordinatesUri(request.getPostalCode()), "", HttpMethod.GET,
				Coordinates.class).onErrorMap(WebClientResponseException.class, ex -> {
					if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
						return new ResourceNotFoundException(
								LOCATION_NOT_FOUND_FOR_POSTAL_CODE, request.getPostalCode());
					}
					return new WeatherServiceException(ERROR_FETCHING_COORDINATES, ex);
				});
	}

	/**
	 * Fetches weather details for the given coordinates.
	 *
	 * @param coordinates The {@link Coordinates} of the location.
	 * @return A {@link Mono<WeatherData>} containing weather information.
	 * @throws ResourceNotFoundException If the location is not found.
	 * @throws WeatherServiceException   If an error occurs while fetching weather data.
	 */
	private Mono<WeatherData> getWeatherDetails(Coordinates coordinates) {
		return webClientService
				.executeRequest(weatherServiceProperties.getUrl(),
						weatherServiceUriUtil.prepareWeatherDataUri(coordinates), "", HttpMethod.GET, WeatherData.class)
				.onErrorMap(WebClientResponseException.class, ex -> {
					if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
						return new ResourceNotFoundException(
								LOCATION_NOT_FOUND_FOR_POSTAL_CODE , coordinates.getCountry());
					}
					return new WeatherServiceException(ERROR_FETCHING_COORDINATES, ex);
				});
	}

	/**
	 * Retrieves weather history for a specific postal code.
	 *
	 * @param postalCode The postal code to search for.
	 * @return A {@link Flux<WeatherData>} emitting historical weather data.
	 */
	@Override
	public Mono<WeatherResponse> getHistoryByPostalCode(String postalCode) {
	    return weatherDataRepository.findByPostalCodeOrderByRequestTimeDesc(postalCode)
	        .map(this::convertToWeatherInfo)
	        .collectList()
	        .map(historyList -> {
	            return mapWeatherResponse(postalCode, null, historyList);
	        });
	}

	/**
	 * Retrieves weather history for a specific username.
	 *
	 * @param username The username to search for.
	 * @return A {@link Flux<WeatherData>} emitting historical weather data.
	 */
	@Override
	public Mono<WeatherResponse> getHistoryByUsername(String username) {
		return weatherDataRepository.findByUsernameOrderByRequestTimeDesc(username)
				.map(this::convertToWeatherInfo)
				.collectList().map(historyList -> {
					return mapWeatherResponse(null ,username, historyList);
				});
	}

	/**
	 * Maps weather data to a response format including current and historical weather information.
	 *
	 * @param postalCode  The postal code of the location
	 * @param username    The username of the requesting user
	 * @param historyList List of historical {@link WeatherInfo} entries
	 * @return {@link WeatherResponse} containing current and historical weather data
	 */
	private WeatherResponse mapWeatherResponse(String postalCode, String username, List<WeatherInfo> historyList) {
		WeatherResponse response = new WeatherResponse();
		response.setPostalCode(postalCode);
		response.setUsername(username);
		response.setTimestamp(LocalDateTime.now());

		if (!historyList.isEmpty()) {
			WeatherInfo currentInfo = historyList.get(0);
			response.setCurrent(currentInfo);
			response.setUsername(currentInfo.getUsername());
		}
		response.setHistory(historyList);
		return response;
	}

	/**
	 * Converts raw weather data into a simplified weather information format.
	 *
	 * @param weatherData The {@link WeatherData} containing raw weather information
	 * @return {@link WeatherInfo} containing formatted weather details
	 */
	private WeatherInfo convertToWeatherInfo(WeatherData weatherData) {
		return WeatherInfo.builder()
				.timestamp(weatherData.getRequestTime())
				.temperature(weatherData.getMain().getTemp())
				.feelsLike(weatherData.getMain().getFeelsLike())
				.humidity(weatherData.getMain().getHumidity())
				.description(weatherData.getWeather().get(0).getDescription())
				.windSpeed(weatherData.getWind().getSpeed())
				.conditions(weatherData.getWeather().get(0).getMain())
				.username(weatherData.getUsername())
				.postalCode(weatherData.getPostalCode())
				.build();
	}
}
