#!/bin/sh
# Arranca el frontend en local: instala dependencias si hace falta y levanta el dev server
# de Vite en primer plano. Ctrl+C para parar.

set -e

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root/frontend"

if [ ! -d "node_modules" ]; then
    echo "Instalando dependencias del frontend..."
    npm install
fi

echo "Arrancando frontend (Ctrl+C para parar)..."
npm run dev
