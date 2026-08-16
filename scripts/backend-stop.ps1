# Para el contenedor de Postgres del backend. Conserva los datos (el volumen no se borra).
# Si la app (spring-boot:run) está corriendo en primer plano en otra terminal, párala ahí con Ctrl+C.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Set-Location $root

Write-Host "Parando Postgres (docker compose)..." -ForegroundColor Cyan
docker compose stop postgres
