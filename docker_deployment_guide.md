# Docker Deployment Guide for SimpleMicroService

This guide provides the instructions to containerize and run your Java 21 Spring Boot application using Docker or Podman.

## 1. Dockerfile Definition

Create a file named `Dockerfile` (no extension) in the root of your project directory, right next to your `pom.xml`, and paste the following multi-stage build configuration:

```dockerfile
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
```

## 2. Building the Image

Open your terminal, navigate to the directory containing your `Dockerfile` and `pom.xml`, and run the following command to build the image:

**Using Docker:**
```bash
docker build -t simple-microservice:1.0.0 .
```

**Using Podman:**
```bash
podman build -t simple-microservice:1.0.0 .
```

## 3. Running the Container

Once the image is built, start a container and map the application's port `8080` to your host machine:

**Using Docker:**
```bash
docker run -d -p 8080:8080 --name simple-app simple-microservice:1.0.0
```

**Using Podman:**
```bash
podman run -d -p 8080:8080 --name simple-app simple-microservice:1.0.0
```

*Note: The `-d` flag runs the container in detached (background) mode.*

## 4. Testing the Deployment

Verify the container is running properly by hitting the health check endpoint:

```bash
curl http://localhost:8080/api/v1/simples/health
```

You should receive a `200 OK` response with a JSON payload indicating the service is up.