# Step 1: Use Java 17 runtime as the base image since that is what i used for the spring boot project
FROM eclipse-temurin:17-jdk-alpine

# Step 2: Set a working directory inside the container
WORKDIR /app

# Step 3: Copy the JAR file from from my target folder into the container
COPY target/campusmarket-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Tell Docker how to run my app
ENTRYPOINT ["java","-jar","app.jar"]
