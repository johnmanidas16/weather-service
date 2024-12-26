package com.weather.service.impl;

import com.weather.dto.WeatherRequest;
import com.weather.dto.WeatherResponse;
import com.weather.model.WeatherData;

import reactor.core.publisher.Mono;

public interface WeatherService {

	/**
	 * 
	 * @param request
	 * @return
	 */
	Mono<WeatherData> getWeatherData(WeatherRequest request);

	/**
	 * 
	 * @param postalCode
	 * @return
	 */
	Mono<WeatherResponse> getHistoryByPostalCode(String postalCode);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<WeatherResponse> getHistoryByUsername(String username);
}
