# Para la app Spring Boot (puerto 8080) si esta corriendo en segundo plano y el contenedor de
# Postgres. Conserva los datos de Postgres (el volumen no se borra).
# Si arrancaste la app en primer plano en esta misma terminal, parala ahi con Ctrl+C.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Set-Location $root

$port = 8080
$conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($conns) {
    $processIds = $conns.OwningProcess | Sort-Object -Unique
    foreach ($processId in $processIds) {
        Write-Host "Parando app Spring Boot, proceso $processId (puerto $port)..." -ForegroundColor Cyan
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
} else {
    Write-Host "No hay nada escuchando en el puerto $port." -ForegroundColor Yellow
}

Write-Host "Parando Postgres (docker compose)..." -ForegroundColor Cyan
docker compose stop postgres

Write-Host "Backend parado." -ForegroundColor Green
