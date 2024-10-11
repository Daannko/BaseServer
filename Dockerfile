# Stage 1: Build the app using Maven
FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR app
ADD pom.xml /app
RUN mvn verify clean --fail-never

COPY . /app
RUN mvn -v
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk-slim
ADD ./target/BaseServer-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/BaseServer-0.0.1-SNAPSHOT.jar"]
