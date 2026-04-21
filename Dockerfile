# --- build stage ---
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# копируем всё сразу (без сложного кеширования)
COPY . .

# собираем jar
RUN gradle clean bootJar -x test

# --- runtime stage ---
FROM eclipse-temurin:21-jdk

WORKDIR /app

# копируем jar из builder
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]