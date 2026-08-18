# Arranca todo en local de un golpe: Postgres (docker-compose) + app Spring Boot en una
# ventana nueva de PowerShell + frontend (Vite) en primer plano en esta ventana.
#
# Ctrl+C en esta ventana para el frontend. La app Spring Boot corre en su propia ventana:
# ciérrala o pulsa Ctrl+C ahí para pararla. Postgres sigue corriendo (usa backend-stop.ps1
# para pararlo, o backend-reset-data.ps1 para empezar de cero).

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Set-Location $root

Write-Host "Levantando Postgres (docker compose)..." -ForegroundColor Cyan
docker compose up -d --wait postgres
if ($LASTEXITCODE -ne 0) {
    Write-Error "Postgres no ha llegado a estar 'healthy'. Revisa 'docker compose logs postgres'."
    exit 1
}

Write-Host "Instalando backend/engine en el repo local de Maven..." -ForegroundColor Cyan
& "$root\mvnw.cmd" install -pl backend/engine -q -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Fallo instalando backend/engine"
    exit 1
}

Write-Host "Arrancando app Spring Boot en una ventana nueva..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$root'; & '$root\mvnw.cmd' org.springframework.boot:spring-boot-maven-plugin:run -f '$root\backend\app\pom.xml'"
)

Write-Host "Esperando a que el backend responda en el puerto 8080..." -ForegroundColor Cyan
$port = 8080
$maxWaitSeconds = 120
$elapsed = 0
$ready = $false
while ($elapsed -lt $maxWaitSeconds) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
}

if ($ready) {
    Write-Host "Backend listo (puerto $port)." -ForegroundColor Green
} else {
    Write-Host "El backend no respondio en $maxWaitSeconds s. Revisa la ventana del backend por si hay un error. Arrancando el frontend igualmente." -ForegroundColor Yellow
}

Set-Location "$root\frontend"

if (-not (Test-Path "node_modules")) {
    Write-Host "Instalando dependencias del frontend..." -ForegroundColor Cyan
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Fallo instalando dependencias del frontend"
        exit 1
    }
}

Write-Host "Arrancando frontend (Ctrl+C para parar)..." -ForegroundColor Cyan
npm run dev
