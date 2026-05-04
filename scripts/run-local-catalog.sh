#!/bin/bash

# ==============================================================================
# run-local-catalog.sh
# Jalankan bidmart-catalog-service untuk local development
# ==============================================================================

set -e

# Pastikan dijalankan dari root project
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo ">>> Project root: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

# ------------------------------------------------------------------------------
# Cek apakah database container hidup
# ------------------------------------------------------------------------------
CONTAINER_NAME="bidmart-catalog-db"

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  echo ""
  echo "ERROR: Container '$CONTAINER_NAME' is not running."
  echo ""
  echo "Jalankan database terlebih dahulu:"
  echo "  ./scripts/setup-local-catalog-db.sh"
  echo ""
  exit 1
fi

echo ">>> Database container '$CONTAINER_NAME' is running. OK"

# ------------------------------------------------------------------------------
# Environment variables untuk local run
# ------------------------------------------------------------------------------
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5435/catalog_db"
export SPRING_DATASOURCE_USERNAME="catalog"
export SPRING_DATASOURCE_PASSWORD="catalog"

export SPRING_RABBITMQ_HOST="localhost"
export SPRING_RABBITMQ_PORT="5672"
export SPRING_RABBITMQ_USERNAME="guest"
export SPRING_RABBITMQ_PASSWORD="guest"

# Secret yang harus sama dengan yang dikonfigurasi di API Gateway
export GATEWAY_SECRET="${GATEWAY_SECRET:-local-dev-gateway-secret}"

export SERVER_PORT="8082"

echo ""
echo "=========================================="
echo "  Starting bidmart-catalog-service"
echo "  Port     : $SERVER_PORT"
echo "  DB URL   : $SPRING_DATASOURCE_URL"
echo "  RabbitMQ : $SPRING_RABBITMQ_HOST:$SPRING_RABBITMQ_PORT"
echo "=========================================="
echo ""

./gradlew bootRun
