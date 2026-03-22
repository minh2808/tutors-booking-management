<<<<<<< HEAD
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

=======
# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml file and download dependencies (caching step)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code and build the Spring Boot app
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (8080 by default for Spring Boot)
EXPOSE 8080

# Run the Spring Boot application
>>>>>>> UPDATE
ENTRYPOINT ["java", "-jar", "app.jar"]
