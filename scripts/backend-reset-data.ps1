# Borra TODOS los datos del Postgres local del backend (para + elimina el volumen de docker-compose)
# y lo vuelve a levantar limpio. La próxima vez que arranques la app, Flyway recrea el esquema
# desde cero (todas las migraciones, sin datos).
#
# Uso: .\scripts\backend-reset-data.ps1        (pide confirmación)
#      .\scripts\backend-reset-data.ps1 -Force  (sin confirmación, para scripts/CI)

param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Set-Location $root

if (-not $Force) {
    $answer = Read-Host "Esto borra TODOS los datos locales de Postgres. ¿Continuar? (s/N)"
    if ($answer -ne "s" -and $answer -ne "S") {
        Write-Host "Cancelado." -ForegroundColor Yellow
        exit 0
    }
}

Write-Host "Parando y borrando el volumen de Postgres..." -ForegroundColor Cyan
docker compose down -v

Write-Host "Levantando Postgres limpio..." -ForegroundColor Cyan
docker compose up -d postgres

Write-Host "Listo. Arranca la app con scripts\backend-start.ps1 para recrear el esquema." -ForegroundColor Green
