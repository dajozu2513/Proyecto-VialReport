# Stage 1: Build fat JAR
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle :app:shadowJar --no-daemon

# Stage 2: Minimal JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
