#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
  echo "Falta .env. Cópialo desde el ejemplo y completa las credenciales Oracle:"
  echo "  cp .env.example .env"
  exit 1
fi

WALLET_DIR="${ORACLE_WALLET_DIR:-$(pwd)/Wallet_ENROLLMENTPLATFORMDB}"
if [[ ! -f "${WALLET_DIR}/tnsnames.ora" ]]; then
  echo "No se encontró la wallet Oracle en: ${WALLET_DIR}"
  echo "Descarga la Instance Wallet desde OCI Console y colócala en Wallet_ENROLLMENTPLATFORMDB/"
  echo "Ver wallet.example/README.md y docs/configuracion-desarrollador.md"
  exit 1
fi

if [[ ! -f "${WALLET_DIR}/cwallet.sso" && ! -f "${WALLET_DIR}/ewallet.pem" ]]; then
  echo "Wallet incompleta en: ${WALLET_DIR}"
  echo "Falta cwallet.sso o ewallet.pem (certificados mTLS de la wallet descargada de OCI)"
  echo "Ver wallet.example/README.md"
  exit 1
fi

export TNS_ADMIN="${WALLET_DIR}"
export ORACLE_WALLET_DIR="${WALLET_DIR}"

set -a
source .env
set +a

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
fi

echo "Perfil: ${SPRING_PROFILES_ACTIVE:-prod}"
echo "Wallet (TNS_ADMIN): ${TNS_ADMIN}"
echo "JDBC: ${SPRING_DATASOURCE_URL:-jdbc:oracle:thin:@enrollmentplatformdb_high}"
./mvnw spring-boot:run
