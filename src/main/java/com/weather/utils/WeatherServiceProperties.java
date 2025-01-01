package com.weather.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "weather.api")
public class  WeatherServiceProperties{

	private String url;
	private String appId;
}
