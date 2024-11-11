# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY /build/libs/fat.jar /app/fat.jar

# Define the command to run your JAR file
CMD ["java", "-jar", "/app/fat.jar"]
