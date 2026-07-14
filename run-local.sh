#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

export SPRING_PROFILES_ACTIVE=local

# .env suele tener Oracle (prod); no debe pisar el datasource H2 del perfil local
unset SPRING_DATASOURCE_URL \
      SPRING_DATASOURCE_USERNAME \
      SPRING_DATASOURCE_PASSWORD \
      SPRING_DATASOURCE_DRIVER_CLASS_NAME \
      TNS_ADMIN \
      ORACLE_WALLET_DIR

export AWS_REGION="${AWS_REGION:-us-east-1}"
export AWS_S3_BUCKET="${AWS_S3_BUCKET:-enrollment-platform-summaries}"
export AWS_S3_ENDPOINT="${AWS_S3_ENDPOINT:-http://localhost:4566}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-test}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-test}"

jwt_enabled="${ENROLLMENT_SECURITY_JWT_ENABLED:-false}"
if [[ "${jwt_enabled}" == "true" || "${jwt_enabled}" == "TRUE" || "${jwt_enabled}" == "1" ]]; then
  export ENROLLMENT_SECURITY_JWT_ENABLED=true
  if [[ -z "${AZURE_B2C_JWK_SET_URI:-}" || -z "${AZURE_B2C_AUDIENCE:-}" ]]; then
    echo "ENROLLMENT_SECURITY_JWT_ENABLED=true requiere AZURE_B2C_JWK_SET_URI y AZURE_B2C_AUDIENCE en .env" >&2
    exit 1
  fi
  jwt_mode="JWT ON (Azure)"
else
  export ENROLLMENT_SECURITY_JWT_ENABLED=false
  unset AZURE_B2C_JWK_SET_URI AZURE_B2C_AUDIENCE
  jwt_mode="JWT OFF"
fi

ensure_rabbitmq() {
  local rabbit_name="rabbitmq"
  if docker inspect "$rabbit_name" >/dev/null 2>&1; then
    local status
    status="$(docker inspect -f '{{.State.Status}}' "$rabbit_name")"
    if [[ "$status" != "running" ]]; then
      echo "RabbitMQ detenido (status=$status). Reiniciando contenedor existente..."
      docker start "$rabbit_name" >/dev/null
    else
      echo "RabbitMQ ya está en ejecución."
    fi
  else
    echo "Creando contenedor RabbitMQ..."
    docker run -d --name "$rabbit_name" -p 5672:5672 -p 15672:15672 rabbitmq:3-management >/dev/null
  fi

  echo "Esperando RabbitMQ en localhost:5672..."
  local attempt=0
  until nc -z 127.0.0.1 5672 2>/dev/null; do
    attempt=$((attempt + 1))
    if (( attempt > 60 )); then
      echo "Timeout: RabbitMQ no respondió en el puerto 5672." >&2
      docker ps -a --filter "name=^${rabbit_name}$" >&2 || true
      exit 1
    fi
    sleep 1
  done
  echo "RabbitMQ listo."
}

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
fi

ensure_rabbitmq

echo "Perfil: local (H2). Seguridad: ${jwt_mode}."
echo "S3: ${AWS_S3_ENDPOINT} / bucket ${AWS_S3_BUCKET}"
if [[ "${ENROLLMENT_SECURITY_JWT_ENABLED}" == "true" ]]; then
  echo "H2 Console: http://localhost:8080/h2-console (sin token). API: Bearer Azure."
fi
./mvnw spring-boot:run
