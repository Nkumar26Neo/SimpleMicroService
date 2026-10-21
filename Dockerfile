FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user and group for security
RUN addgroup -S springgroup && adduser -S springuser -G springgroup
USER springuser:springgroup

# Copy the compiled JAR  and rename it to app.jar
COPY target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]