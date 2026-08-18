#!/bin/sh
# Borra TODOS los datos del Postgres local del backend (para + elimina el volumen de docker-compose)
# y lo vuelve a levantar limpio. La proxima vez que arranques la app, Flyway recrea el esquema
# desde cero (todas las migraciones, sin datos).
#
# Uso: ./scripts/backend-reset-data.sh          (pide confirmacion)
#      ./scripts/backend-reset-data.sh --force  (sin confirmacion, para scripts/CI)

set -e

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

force=0
if [ "$1" = "--force" ] || [ "$1" = "-f" ]; then
    force=1
fi

if [ "$force" -eq 0 ]; then
    printf "Esto borra TODOS los datos locales de Postgres. ¿Continuar? (s/N) "
    read -r answer
    case "$answer" in
        s|S)
            ;;
        *)
            echo "Cancelado."
            exit 0
            ;;
    esac
fi

echo "Parando y borrando el volumen de Postgres..."
docker compose down -v

echo "Levantando Postgres limpio..."
docker compose up -d postgres

echo "Listo. Arranca la app con scripts/backend-start.sh para recrear el esquema."
