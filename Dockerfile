FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven
RUN mvn dependency:purge-local-repository -DreResolve=true
RUN mvn clean package -DskipTests

RUN cp target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]