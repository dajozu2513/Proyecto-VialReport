# Stage 1: Build fat JAR using the Gradle wrapper (downloads Gradle 9.3.1)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew :app:shadowJar --no-daemon

# Stage 2: Minimal JRE 21 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
