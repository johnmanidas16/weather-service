package com.weather.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.weather.model.WeatherData;

import reactor.core.publisher.Flux;

@Repository
public interface WeatherDataRepository extends ReactiveMongoRepository<WeatherData, UUID> {
	
	Flux<WeatherData> findByPostalCodeOrderByRequestTimeDesc(String postalCode);
	Flux<WeatherData> findByUsernameOrderByRequestTimeDesc(String username);
}
