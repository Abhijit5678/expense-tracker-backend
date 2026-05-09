FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy Maven files first
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Force fresh dependency download
RUN ./mvnw dependency:purge-local-repository -DreResolve=true

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Copy generated jar
RUN cp target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]