# syntax=docker/dockerfile:1

# ---- Build stage: compile and package with JDK 25 + Maven ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Resolve dependencies first for better layer caching.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build the application (tests are run separately via `mvn verify`).
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage: slim JRE image ----
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/target/transfer-module-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
