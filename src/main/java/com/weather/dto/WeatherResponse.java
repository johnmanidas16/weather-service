package com.weather.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class WeatherResponse {

	private String postalCode;
	private String username;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;
	private WeatherInfo current;
	private List<WeatherInfo> history;
}
