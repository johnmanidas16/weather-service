package com.weather.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherInfo {

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;
	private double temperature;
	private double feelsLike;
	private int humidity;
	private String description;
	private double windSpeed;
	private String conditions;
	private String username;
	private String postalCode;
}
