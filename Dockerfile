# Build stage
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

# Copy only pom.xml first for better layer caching
COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]