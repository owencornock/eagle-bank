# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

# Copy the entire project
COPY . .

# Build the application
RUN ./gradlew :eagle-bank-app:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21.0.1_12-jre
WORKDIR /app

# Copy the jar from builder
COPY --from=builder /workspace/eagle-bank-app/build/libs/*.jar app.jar

# Copy the application.yml
COPY src/main/resources/application.yml /app/application.yml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]