# Stage 1: Build fat JAR using the official Gradle image (no wrapper jar needed)
FROM gradle:8.10.2-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle :app:shadowJar --no-daemon

# Stage 2: Minimal JRE 21 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
