# ========================#
# INFO: Application Build
# ========================#
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# =====================#
# INFO: RUNTIME IMAGE
# =====================#
# JRE em Ubuntu (não alpine): o FFmpeg do apt traz libx264 e o filtro scale com force_divisible_by (FFmpeg >= 5).
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar api.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api.jar"]
