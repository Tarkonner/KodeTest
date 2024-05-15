# Use an official Maven image as the base image
FROM maven:3.8.4-openjdk-11-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file to the working directory
COPY pom.xml /app/

# Copy the rest of the source code to the working directory
COPY app/src/ /app/

# Build the application using Maven
RUN mvn clean package -DskipTests

# Use an official OpenJDK image as the base image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/demo-1.0-SNAPSHOT.jar /app/demo.jar

# Set the command to run the application
CMD ["java", "-jar", "/app/demo.jar"]