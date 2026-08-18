#!/bin/sh
# Arranca todo en local de un golpe: Postgres (docker-compose) + app Spring Boot en
# segundo plano + frontend (Vite) en primer plano.
#
# Funciona en Linux, macOS y Windows con Git Bash (viene con Git, que ya hace falta
# para clonar el repo).
#
# Ctrl+C para el frontend en primer plano; al salir, este script para tambien la app
# Spring Boot que lanzo en segundo plano. Postgres sigue corriendo (usa
# backend-stop.ps1 o "docker compose stop postgres" si quieres pararlo tambien).

set -e

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

BACKEND_PID=""

cleanup() {
    if [ -n "$BACKEND_PID" ] && kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "Parando la app Spring Boot (pid $BACKEND_PID)..."
        kill "$BACKEND_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

echo "Levantando Postgres (docker compose)..."
docker compose up -d --wait postgres

echo "Instalando backend/engine en el repo local de Maven..."
./mvnw install -pl backend/engine -q -DskipTests

echo "Arrancando app Spring Boot en segundo plano..."
./mvnw org.springframework.boot:spring-boot-maven-plugin:run -f "$root/backend/app/pom.xml" -q \
    > "$root/backend-app.log" 2>&1 &
BACKEND_PID=$!

echo "Esperando a que el backend responda en el puerto 8080 (log: backend-app.log)..."
max_wait=120
elapsed=0
ready=0
while [ "$elapsed" -lt "$max_wait" ]; do
    if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "La app Spring Boot se ha parado inesperadamente. Revisa backend-app.log."
        exit 1
    fi
    if (exec 3<>"/dev/tcp/localhost/8080") 2>/dev/null; then
        exec 3<&- 3>&-
        ready=1
        break
    fi
    sleep 2
    elapsed=$((elapsed + 2))
done

if [ "$ready" -eq 1 ]; then
    echo "Backend listo (puerto 8080)."
else
    echo "El backend no respondio en ${max_wait}s. Revisa backend-app.log. Arrancando el frontend igualmente."
fi

cd "$root/frontend"

if [ ! -d "node_modules" ]; then
    echo "Instalando dependencias del frontend..."
    npm install
fi

echo "Arrancando frontend (Ctrl+C para parar)..."
npm run dev
