package com.weather.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorConstants {

    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String REQUEST_ERROR = "Request Error";
    public static final String SERVICE_ERROR = "Service Error";
    public static final String SERVICE_TEMPORARILY_UNAVAILABLE = "Service temporarily unavailable";
    public static final String EXTERNAL_SERVICE_ERROR = "External Service Error";
    public static final String RESOURCE_NOT_FOUND = "Resource Not Found";
    public static final String INVALID_REQUEST = "Invalid Request";
    public static final String AUTHENTICATION_FAILED = "Authentication Failed";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    public static final String AUTHORIZATION_FAILED = "Authorization Failed";
    public static final String INVALID_REQUEST_MISSING_POSTAL_CODE = "Invalid request: missing postal code";
    public static final String INVALID_POSTAL_CODE_FORMAT = "Invalid postal code format";
    public static final String DATABASE_ERROR_WHILE_SAVING_WEATHER_DATA = "Database error while saving weather data";
    public static final String ACCESS_DENIED_YOU_CAN_ONLY_ACCESS_YOUR_OWN_WEATHER_DATA = "Access denied. You can only access your own weather data.";
    public static final String LOCATION_NOT_FOUND_FOR_POSTAL_CODE = "Location not found for postal code: ";
    public static final String ERROR_FETCHING_COORDINATES = "Error fetching coordinates";
    public static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists: ";
    public static final String USER_NOT_FOUND = "User not found: ";
}
