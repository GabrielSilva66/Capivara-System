#!/usr/bin/env bash
# build-all.sh – Faz o package Maven de todos os serviços antes do compose build
# Uso: ./build-all.sh
set -euo pipefail

SERVERS_DIR="$(cd "$(dirname "$0")" && pwd)"

services=(
  server-config
  eureka
  gateway
  capiva-core
  capiva-ai
  capiva-scalator
)

echo "══════════════════════════════════════════════════"
echo "  SystemCapivara – Build Maven de todos os módulos"
echo "══════════════════════════════════════════════════"

for svc in "${services[@]}"; do
  dir="$SERVERS_DIR/$svc"
  echo ""
  echo "▶ Building: $svc"
  (cd "$dir" && ./mvnw package -DskipTests -q)
  echo "  ✓ $svc concluído"
done

echo ""
echo "══════════════════════════════════════════════════"
echo "  Todos os builds concluídos. Execute agora:"
echo "  podman compose up -d --build"
echo "══════════════════════════════════════════════════"
