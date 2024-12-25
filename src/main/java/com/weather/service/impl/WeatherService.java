package com.weather.service.impl;

import com.weather.dto.WeatherRequest;
import com.weather.model.WeatherData;

import reactor.core.publisher.Flux;
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
	Flux<WeatherData> getHistoryByPostalCode(String postalCode);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Flux<WeatherData> getHistoryByUsername(String username);
}
