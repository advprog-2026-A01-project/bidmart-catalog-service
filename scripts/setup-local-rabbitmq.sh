#!/bin/bash

# ==============================================================================
# setup-local-rabbitmq.sh
# Jalankan RabbitMQ lokal via Docker (shared dengan auction-service)
# Catatan: Jalankan script ini sekali saja, dipakai bersama oleh
#          catalog-service dan auction-wallet-service
# ==============================================================================

set -e

CONTAINER_NAME="bidmart-rabbitmq"
RABBITMQ_USER="guest"
RABBITMQ_PASSWORD="guest"
AMQP_PORT="5672"
MANAGEMENT_PORT="15672"

echo ">>> Removing existing container (if any)..."
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

echo ">>> Starting RabbitMQ container: $CONTAINER_NAME"
docker run --name "$CONTAINER_NAME" \
  -e RABBITMQ_DEFAULT_USER="$RABBITMQ_USER" \
  -e RABBITMQ_DEFAULT_PASS="$RABBITMQ_PASSWORD" \
  -p "$AMQP_PORT:5672" \
  -p "$MANAGEMENT_PORT:15672" \
  -d rabbitmq:3-management

echo ""
echo ">>> Waiting for RabbitMQ to be ready..."
sleep 5

echo ""
echo ">>> Container status:"
docker ps | grep "$CONTAINER_NAME" || echo "WARNING: Container not found in docker ps"

echo ""
echo "=========================================="
echo "  RabbitMQ is running!"
echo "  AMQP     : localhost:$AMQP_PORT"
echo "  UI Admin : http://localhost:$MANAGEMENT_PORT"
echo "  User     : $RABBITMQ_USER"
echo "  Password : $RABBITMQ_PASSWORD"
echo "=========================================="
echo ""
echo "Buka Management UI di browser:"
echo "  http://localhost:$MANAGEMENT_PORT"
echo "  Login: guest / guest"
