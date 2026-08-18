#!/bin/sh
# Para la app Spring Boot (puerto 8080) si esta corriendo en segundo plano y el contenedor de
# Postgres. Conserva los datos de Postgres (el volumen no se borra).
# Si arrancaste la app en primer plano en esta misma terminal, parala ahi con Ctrl+C.

set -e

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

port=8080
pid=""

if command -v lsof >/dev/null 2>&1; then
    pid=$(lsof -ti tcp:"$port" 2>/dev/null || true)
elif command -v netstat >/dev/null 2>&1; then
    if netstat -ano 2>/dev/null | grep -q "0.0.0.0"; then
        pid=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}' | sort -u)
    else
        pid=$(netstat -anp 2>/dev/null | grep ":$port " | grep LISTEN | sed -E 's#.*LISTEN[^0-9]*([0-9]+)/.*#\1#' | sort -u)
    fi
fi

if [ -n "$pid" ]; then
    for p in $pid; do
        echo "Parando app Spring Boot, proceso $p (puerto $port)..."
        kill "$p" 2>/dev/null || taskkill //PID "$p" //F >/dev/null 2>&1 || true
    done
else
    echo "No hay nada escuchando en el puerto $port (o no se pudo detectar: instala lsof o netstat)."
fi

echo "Parando Postgres (docker compose)..."
docker compose stop postgres

echo "Backend parado."
