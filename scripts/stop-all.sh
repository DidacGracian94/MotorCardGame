#!/bin/sh
# Para todo: frontend (puerto 5173), app Spring Boot (puerto 8080) y Postgres (docker compose).
# Combina backend-stop.sh + frontend-stop.sh en un solo paso.

set -e

root="$(cd "$(dirname "$0")" && pwd)"

sh "$root/frontend-stop.sh"
sh "$root/backend-stop.sh"
