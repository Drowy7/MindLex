# MindLex — сборка и запуск бэкенда.
# Сборка (фактор V) отделена от запуска: в образ попадает только собранный jar.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- стадия запуска ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S mindlex && adduser -S mindlex -G mindlex
COPY --from=build /app/target/mindlex-backend.jar /app/app.jar
USER mindlex

# Фактор VII: приложение само публикует HTTP-порт
EXPOSE 8080

# Фактор IX: корректное завершение по SIGTERM с ограничением по времени
STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
