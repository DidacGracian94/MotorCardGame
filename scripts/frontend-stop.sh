#!/bin/sh
# Para el dev server del frontend (Vite, puerto 5173) si esta corriendo en segundo plano.
# Si lo arrancaste en primer plano con frontend-start.sh o start-all.sh, paralo ahi con Ctrl+C.

port=5173
pid=""

if command -v lsof >/dev/null 2>&1; then
    pid=$(lsof -ti tcp:"$port" 2>/dev/null || true)
elif command -v netstat >/dev/null 2>&1; then
    # Windows (Git Bash) y Linux/macOS sin lsof: parsear netstat.
    if netstat -ano 2>/dev/null | grep -q "0.0.0.0"; then
        # Formato Windows: ... 0.0.0.0:5173 ... LISTENING <PID>
        pid=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}' | sort -u)
    else
        # Formato Linux/macOS: ... tcp ... :5173 ... LISTEN <PID>/programa
        pid=$(netstat -anp 2>/dev/null | grep ":$port " | grep LISTEN | sed -E 's#.*LISTEN[^0-9]*([0-9]+)/.*#\1#' | sort -u)
    fi
fi

if [ -z "$pid" ]; then
    echo "No hay nada escuchando en el puerto $port (o no se pudo detectar: instala lsof o netstat)."
    exit 0
fi

for p in $pid; do
    echo "Parando proceso $p (puerto $port)..."
    kill "$p" 2>/dev/null || taskkill //PID "$p" //F >/dev/null 2>&1 || true
done

echo "Frontend parado."
