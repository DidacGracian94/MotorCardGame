#!/bin/sh
# Arranca el backend en local: Postgres (docker-compose) + app Spring Boot en primer plano.
# Ctrl+C para parar la app. Los datos de Postgres persisten entre arranques (usa
# backend-reset-data.sh si quieres empezar de cero).

set -e

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

echo "Levantando Postgres (docker compose)..."
docker compose up -d --wait postgres

echo "Instalando backend/engine en el repo local de Maven..."
./mvnw install -pl backend/engine -q -DskipTests

echo "Arrancando app (Ctrl+C para parar)..."
./mvnw org.springframework.boot:spring-boot-maven-plugin:run -f "$root/backend/app/pom.xml" -q
