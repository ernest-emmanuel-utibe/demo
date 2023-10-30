FROM openjdk:11-jre-slim

# Set the working directory to /app
WORKDIR /app

# Copy the application JAR file into the container at /app
COPY target/demo-0.0.1-SNAPSHOT.jar .

# Run the application when the container launches
CMD ["java", "-jar", "demo.jar"]