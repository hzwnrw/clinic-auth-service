FROM openjdk:17
WORKDIR /app
# Copy the JAR file with a wildcard to match the dynamic version
COPY target/auth-service-*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]