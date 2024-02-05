# Build Stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Install curl in the Maven image for build purposes
RUN apk --no-cache add curl

COPY pom.xml .
COPY src /app/src
RUN mvn -T 1C --batch-mode --quiet package -DskipTests

# Final Stage
FROM amazoncorretto:21.0.2

WORKDIR /app

# Optionally, create the cache directory and set proper permissions
RUN mkdir /app/cache && chown 1000:1000 /app/cache

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Install curl using yum
RUN yum install -y curl

# Set the timezone for the JVM
ENV JAVA_OPTS="-Xmx600m -Xms300m -Duser.timezone=Europe/Tallinn"

# Set the command to run your application with JAVA_OPTS
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
