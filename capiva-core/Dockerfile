# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM docker.io/eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM docker.io/eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S capivara && adduser -S capivara -G capivara
USER capivara

COPY --from=builder /app/target/*.jar app.jar

# A porta é definida pelo compose via SERVER_PORT (padrão 8080 do Spring)
# As duas instâncias usarão 8081 e 8083
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
