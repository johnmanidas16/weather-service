# Weather Service API

A reactive Spring WebFlux application that provides weather information based on US postal codes with JWT authentication.

## Features

- Fetch weather data using postal codes
- User registration and authentication using JWT
- Weather history tracking by postal code and username
- Reactive MongoDB integration
- OpenAPI/Swagger documentation
- Docker support

## Technologies

- Java 17
- Spring Boot 3.x
- Spring WebFlux
- Spring Security
- MongoDB
- JWT Authentication
- OpenWeatherMap API
- Swagger/OpenAPI

## Prerequisites

- JDK 17
- MongoDB 4.4+
- Maven 3.8+
- Docker (optional)

## Configuration

# Security

- JWT-based authentication
- Password encryption using BCrypt
- Protected endpoints requiring authentication
- User activation/deactivation support

## Error Handling
The application includes comprehensive error handling for:
- Invalid postal codes
- Invalid JWT tokens
- User authentication failures
- Weather API integration issues
- Database errors

## Build the project
mvn clean install

## Run the application
mvn spring-boot:run

## API Documentation
- Access Swagger UI at: http://localhost:8084/swagger-ui.html

## Authentication
- POST /api/auth/register
- POST /api/auth/login
- PUT /api/auth/users/{username}/activate
- PUT /api/auth/users/{username}/deactivate

## Weather Data
- POST /api/weather/info
- GET /api/weather/history/postal-code/{postalCode}
- GET /api/weather/history/user/{username}

## Application Properties
Create `application.yml` in `src/main/resources`:

```yaml
server:
  port: 8084

spring:
  application:
    name: weather-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/weatherdb
      auto-index-creation: false
  webflux:
    base-path: ""

weather:
  api:
    url: https://api.openweathermap.org
    app-id: YOUR_OPENWEATHER_API_KEY

jwt:
  secret: "YOUR_JWT_SECRET_KEY"

springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui
    config-url: /v3/api-docs/swagger-config
    url: /v3/api-docs
    disable-swagger-default-url: true
  webjars:
    prefix: /webjars
  use-management-port: false
```

## Local Development
## Build the project
mvn clean install

## Run the application
mvn spring-boot:run

## Build Docker image
docker build -t weather-service .

## Run with Docker Compose
docker-compose up

