# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy only the pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .
# Download dependencies offline to speed up subsequent builds
RUN mvn dependency:go-offline -B

# Copy the actual source code and build the executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Create the lightweight production image
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user and group for security
RUN addgroup -S springgroup && adduser -S springuser -G springgroup
USER springuser:springgroup

# Copy the compiled JAR from the builder stage and rename it to app.jar
COPY --from=builder /build/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]