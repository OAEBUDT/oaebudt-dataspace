# Stage 1: Build the JAR using Gradle
FROM eclipse-temurin:23.0.2_7-jdk-alpine AS builder

WORKDIR /app

# Copy the Gradle configuration files
COPY ./gradlew  ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./
COPY ./gradle ./gradle

# Make gradlew executable and download dependencies as a separate step to leverage Docker cache
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# Copy connector sources
COPY ./launchers/controlplane ./launchers/controlplane
COPY ./launchers/dataplane ./launchers/dataplane
COPY ./launchers/runtime-embedded ./launchers/runtime-embedded

# Run Gradle to build the JAR with parallel execution for faster builds
RUN ./gradlew clean :launchers:runtime-embedded:build --parallel --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:23.0.2_7-jre-alpine

WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S connector && adduser -S connector -G connector
USER connector

# Copy the JAR from the builder stage
COPY --from=builder /app/launchers/runtime-embedded/build/libs/*.jar connector.jar

# Set environment variables
ENV WEB_HTTP_PORT="8080" \
    WEB_HTTP_PATH="/api" \
    JVM_ARGS=""

EXPOSE ${WEB_HTTP_PORT}

HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD wget --spider --quiet --timeout=5 --tries=1 http://localhost:${WEB_HTTP_PORT}${WEB_HTTP_PATH}/check/health || exit 1

# Use "exec" for graceful termination (SIGINT) to reach JVM.
ENTRYPOINT [ "sh", "-c", "exec java ${JVM_ARGS} -jar connector.jar --log-level=debug"]
