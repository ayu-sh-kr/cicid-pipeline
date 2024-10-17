# Use an official OpenJDK runtime as a parent image
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21
COPY --from=build /target/cicid-pipeline-0.0.1-SNAPSHOT.jar myapp.jar
COPY --from=build /target/classes /target/classes

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "myapp.jar"]