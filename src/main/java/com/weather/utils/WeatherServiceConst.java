package com.weather.utils;

public interface WeatherServiceConst {

	public static String GEO_COORDINATES_URI = "/geo/1.0/zip?zip=POSTAL_CODE,COUNTRY_CODE&appid=API_KEY";
	public static String WEATHER_DATA_URI = "/data/2.5/weather?lat=LAT&lon=LON&appid=API_KEY";
	public static final String TYPE = "type";
	public static final String ROLES = "roles";
	public static final String ISSUER = "issuer";
	public static final String CREATED = "created";
	public static final String AUDIENCE = "audience";
	public static final String WEATHER_API = "weather-api";
	public static final String WEATHER_SERVICE = "weather-service";
	public static final String ROLE_USER = "ROLE_USER";
	public static final String BEARER = "Bearer";
}
