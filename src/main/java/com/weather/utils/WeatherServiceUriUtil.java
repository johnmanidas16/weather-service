package com.weather.utils;

import static com.weather.utils.WeatherServiceConst.GEO_COORDINATES_URI;
import static com.weather.utils.WeatherServiceConst.WEATHER_DATA_URI;

import org.springframework.stereotype.Component;

import com.weather.dto.Coordinates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for preparing URIs for the Weather Service.
 * Handles the generation of URIs for fetching geo-coordinates and weather data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherServiceUriUtil {

    public static final String US = "US";
    private final WeatherServiceProperties weatherServiceProperties;
	
	// Placeholder keys used in URI templates
    private static final String LON = "LON";
	private static final String LAT = "LAT";
	private static final String COUNTRY_CODE = "COUNTRY_CODE";
	private static final String POSTAL_CODE = "POSTAL_CODE";
	private static final String API_KEY = "API_KEY";

	/**
     * Prepares the URI for fetching geo-coordinates based on the postal code.
     *
     * @param postalCode The postal code for which the geo-coordinates are required.
     * @return A fully prepared URI with the postal code, country code, and API key injected.
     */
    public String prepareGeoCoordinatesUri(String postalCode) {
        return GEO_COORDINATES_URI
            .replace(POSTAL_CODE, postalCode)
            .replace(COUNTRY_CODE, US)
            .replace(API_KEY, weatherServiceProperties.getAppId());
    }

    /**
     * Prepares the URI for fetching weather data based on geo-coordinates.
     *
     * @param coordinates An instance of {@link Coordinates} containing latitude and longitude.
     * @return A fully prepared URI with the latitude, longitude, and API key injected.
     */
    public String prepareWeatherDataUri(Coordinates coordinates) {
        return WEATHER_DATA_URI
            .replace(LAT, String.valueOf(coordinates.getLat()))
            .replace(LON, String.valueOf(coordinates.getLon()))
            .replace(API_KEY, weatherServiceProperties.getAppId());
    }
}