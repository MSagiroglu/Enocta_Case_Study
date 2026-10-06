FROM maven:3.9.5-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
# Download dependencies for offline usage
RUN mvn dependency:go-offline
COPY src ./src
