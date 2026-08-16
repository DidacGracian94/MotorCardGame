# Para el dev server del frontend (Vite, puerto 5173) si está corriendo en segundo plano.
# Si lo arrancaste en primer plano con frontend-start.ps1, párralo ahí con Ctrl+C.

$ErrorActionPreference = "Stop"
$port = 5173

$conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if (-not $conns) {
    Write-Host "No hay nada escuchando en el puerto $port." -ForegroundColor Yellow
    exit 0
}

$processIds = $conns.OwningProcess | Sort-Object -Unique
foreach ($processId in $processIds) {
    Write-Host "Parando proceso $processId (puerto $port)..." -ForegroundColor Cyan
    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
}

Write-Host "Frontend parado." -ForegroundColor Green
