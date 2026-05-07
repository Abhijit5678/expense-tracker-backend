FROM eclipse-temurin:17-jdk-jammy
LABEL authors="abhij"
WORKDIR /app

# Copy the built JAR file
COPY target/expense-backend-0.0.1-SNAPSHOT.jar /app/expense-backend.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/expense-backend.jar"]
