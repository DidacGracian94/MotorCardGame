# Arranca el backend en local: Postgres (docker-compose) + app Spring Boot en primer plano.
# Ctrl+C para parar la app. Los datos de Postgres persisten entre arranques (usa
# backend-reset-data.ps1 si quieres empezar de cero).

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Set-Location $root

Write-Host "Levantando Postgres (docker compose)..." -ForegroundColor Cyan
docker compose up -d postgres

Write-Host "Instalando backend/engine en el repo local de Maven..." -ForegroundColor Cyan
& "$root\mvnw.cmd" install -pl backend/engine -q -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Fallo instalando backend/engine"
    exit 1
}

Write-Host "Arrancando app (Ctrl+C para parar)..." -ForegroundColor Cyan
& "$root\mvnw.cmd" org.springframework.boot:spring-boot-maven-plugin:run -f "$root\backend\app\pom.xml" -q
