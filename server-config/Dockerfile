# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM docker.io/eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia os descritores do Maven primeiro para aproveitar o cache de camadas
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -q

# Copia o código-fonte e faz o build
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM docker.io/eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuário não-root por boas práticas de segurança
RUN addgroup -S capivara && adduser -S capivara -G capivara
USER capivara

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
