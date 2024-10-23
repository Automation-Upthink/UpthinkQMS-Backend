# From a larger Ubuntu image
FROM 462203881643.dkr.ecr.ap-south-1.amazonaws.com/qms-base-image:latest


# Set the working directory in the container
WORKDIR /app


# Copy the JAR file from the build directory to the container
COPY build/libs/QMS-SNAPSHOT-1.0.jar app.jar

# Expose the port that your Spring Boot app runs on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]