# Para todo: frontend (puerto 5173), app Spring Boot (puerto 8080) y Postgres (docker compose).
# Combina backend-stop.ps1 + frontend-stop.ps1 en un solo paso.

$ErrorActionPreference = "Stop"

& "$PSScriptRoot\frontend-stop.ps1"
& "$PSScriptRoot\backend-stop.ps1"
