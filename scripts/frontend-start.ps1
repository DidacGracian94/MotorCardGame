# Arranca el frontend en local: instala dependencias si hace falta y levanta el dev server
# de Vite en primer plano. Ctrl+C para parar.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

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
