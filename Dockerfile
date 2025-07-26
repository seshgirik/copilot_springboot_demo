FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/springboot-grpc-rest-demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the ports
EXPOSE 8080 9090

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
