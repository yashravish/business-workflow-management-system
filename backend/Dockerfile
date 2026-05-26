# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Resolve dependencies first so this layer is cached across source-only changes
COPY pom.xml .
RUN mvn -B dependency:resolve -q

COPY src ./src
RUN mvn -B package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S bwms && adduser -S bwms -G bwms
WORKDIR /app

COPY --from=builder --chown=bwms:bwms /build/target/*.jar app.jar

USER bwms
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
