# ============================================
# Stage 1: Build the application with Maven
# ============================================
FROM maven:3.9-amazoncorretto-21 AS build

LABEL author="Abe Evans"
LABEL description="NMS Cinemas - Build Stage"

# Set working directory
WORKDIR /app

# Copy pom.xml first (for Docker layer caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Create the runtime image
# ============================================
FROM amazoncorretto:21-alpine

LABEL author="Abe Evans"
LABEL description="NMS Cinemas Movie Ticket Booking System - Runtime"
LABEL version="0.0.1-SNAPSHOT"

# Create non-root user for security
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]