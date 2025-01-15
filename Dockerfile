# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory to /app
WORKDIR /app

# Copy the WAR file from the local filesystem to the container at /app
COPY target/lugha-translator.war /app/lugha-translator.war


# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the application using java -jar command with the WAR file and JVM options
CMD java $JAVA_OPTS -jar lugha-translator.war

