# Stage 1: Build frontend
FROM node:22-alpine AS frontend-build

WORKDIR /frontend

# Copy frontend package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci

# Copy frontend source
COPY frontend/ ./

# Build frontend (outputs to ../src/main/resources/static)
RUN npm run build

# Stage 2: Build backend
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Copy built frontend from stage 1
COPY --from=frontend-build /src/main/resources/static src/main/resources/static

# Build backend (skip tests for faster build)
RUN ./gradlew bootJar -x test

# Stage 3: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built jar from stage 2
COPY --from=backend-build /app/build/libs/*.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
