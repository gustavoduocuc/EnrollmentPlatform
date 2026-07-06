# Configuración por desarrollador

Cada desarrollador o fork trabaja con **su propia** base de datos Oracle y secrets. El repositorio no incluye wallets ni credenciales reales.

---

## Checklist: primer clone (fork)

1. Clonar el repositorio.
2. Copiar plantilla de entorno: `cp .env.example .env`
3. Descargar tu Instance Wallet desde OCI Console (ver sección [Wallet Oracle](#wallet-oracle)).
4. Completar `.env` con usuario, contraseña, alias TNS de **tu** instancia y variables RabbitMQ.
5. Levantar RabbitMQ y LocalStack (ver [Procesamiento asíncrono MQ](procesamiento-asincrono-mq.md)).
6. Para desarrollo sin Oracle: `./run-local.sh` (H2 en memoria).
7. Para probar contra Oracle: `./run-prod.sh`.
8. Si despliegas tu fork a EC2: configurar secrets en GitHub Actions ([guia-despliegue-ec2.md](guia-despliegue-ec2.md)).

---

## Wallet Oracle

### Descarga desde OCI

1. OCI Console → **Autonomous Database** → tu instancia.
2. **Database connection** → **Download wallet (Instance Wallet)**.
3. Descomprime el zip. Oracle crea una carpeta como `Wallet_<NOMBRE_BD>/`.
4. Coloca el contenido en `Wallet_ENROLLMENTPLATFORMDB/` en la raíz del proyecto:

```bash
mkdir -p Wallet_ENROLLMENTPLATFORMDB
# Copia todos los archivos de tu wallet descargada a esa carpeta
```

**Alternativa:** guarda la wallet en otra ruta y define en `.env`:

```bash
ORACLE_WALLET_DIR=/ruta/absoluta/a/tu/wallet
```

### Archivos requeridos

| Archivo | Obligatorio |
| ------- | ----------- |
| `tnsnames.ora` | Sí |
| `sqlnet.ora` | Sí |
| `cwallet.sso` o `ewallet.pem` | Sí (mTLS) |

Plantillas genéricas de referencia: carpeta [`wallet.example/`](../wallet.example/).

### Alias TNS y URL JDBC

Los alias en `tnsnames.ora` son **únicos por instancia Oracle**. No copies el de otro desarrollador.

Lista los alias de tu wallet:

```bash
grep '^[a-z]' Wallet_ENROLLMENTPLATFORMDB/tnsnames.ora
```

Configura en `.env`:

```bash
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<tu_alias>_high
```

Ejemplo si tu alias es `miforkdb_high`:

```bash
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@miforkdb_high
```

---

## Archivo `.env`

```bash
cp .env.example .env
```

Variables clave para perfil `prod`:

| Variable | Descripción |
| -------- | ----------- |
| `SPRING_PROFILES_ACTIVE` | `local` (H2) o `prod` (Oracle) |
| `SPRING_DATASOURCE_URL` | Alias de **tu** `tnsnames.ora` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de tu BD |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de tu BD |
| `ORACLE_WALLET_DIR` | Opcional; ruta a tu wallet |
| `RABBITMQ_HOST` | `localhost` (app nativa) o `rabbitmq` (Docker Compose) |
| `RABBITMQ_PORT` | `5672` (default) |
| `RABBITMQ_USER` / `RABBITMQ_PASS` | Credenciales RabbitMQ (default: `guest` / `guest`) |

Para Docker local, usa `.env.docker.example` como base (incluye `RABBITMQ_HOST=rabbitmq`).

Guía de prueba del flujo MQ: [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md).

---

## RabbitMQ (requerido para inscripciones)

`POST /enrollments` publica un mensaje en RabbitMQ. Sin RabbitMQ levantado, la inscripción puede fallar.

**Levantar RabbitMQ (local nativo):**

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Consola web: http://localhost:15672 (`guest` / `guest`).

En `.env` para `./run-prod.sh` o `./run-local.sh`:

```bash
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
```

Con **Docker Compose**, usa `RABBITMQ_HOST=rabbitmq` (ver `.env.docker.example`).

---

## Qué NO commitear

| Archivo / carpeta | Motivo |
| ----------------- | ------ |
| `.env` | Credenciales personales |
| `Wallet_ENROLLMENTPLATFORMDB/` | Wallet y `tnsnames.ora` de tu instancia |
| `wallet.zip` | Zip de wallet |
| Archivos `.pem` (llaves SSH) | Acceso a servidores |

Estos paths están en `.gitignore`. Si git te pide commitear cambios en la wallet, **no lo hagas**.

---

## `run-local.sh` vs `run-prod.sh`

| Script | Perfil | Base de datos | Wallet Oracle |
| ------ | ------ | ------------- | ------------- |
| `./run-local.sh` | `local` | H2 en memoria | No requerida (ignora vars Oracle del `.env`) |
| `./run-prod.sh` | `prod` (o valor en `.env`) | Oracle Autonomous | Requerida |

Ambos scripts requieren **RabbitMQ** levantado para `POST /enrollments`. Ver [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md).

`run-prod.sh` valida que existan `tnsnames.ora` y `cwallet.sso`/`ewallet.pem` antes de arrancar.

---

## Docker local

`docker-compose.yml` monta tu wallet local en `/app/wallet`:

```yaml
volumes:
  - ${ORACLE_WALLET_DIR:-./Wallet_ENROLLMENTPLATFORMDB}:/app/wallet:ro
```

Prerrequisitos:

1. Wallet descargada en `Wallet_ENROLLMENTPLATFORMDB/` (o ruta en `ORACLE_WALLET_DIR`).
2. `.env` con credenciales Oracle y AWS (ver `.env.docker.example`).

```bash
docker compose up -d localstack
# Crear bucket S3 en LocalStack si aplica (ver README)
docker compose up -d --build
```

El `docker build` aún copia la wallet al construir la imagen (necesaria para CI). En runtime, el volumen usa **tu** wallet local.

---

## Secrets de GitHub por fork

Cada fork con despliegue a EC2 configura **sus propios** secrets en **Settings → Secrets and variables → Actions**:

- `ORACLE_WALLET_BASE64` — zip de **tu** `Wallet_ENROLLMENTPLATFORMDB/` en base64
- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_URL` — alias de **tu** `tnsnames.ora`
- `SPRING_RABBITMQ_HOST` — IP privada de EC2 donde corre RabbitMQ (ver [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md))
- AWS, Docker Hub, EC2, JWT Azure (ver tabla completa en [guia-despliegue-ec2.md](guia-despliegue-ec2.md))

Si cambias de instancia Oracle, regenera `ORACLE_WALLET_BASE64`:

```bash
zip -r wallet.zip Wallet_ENROLLMENTPLATFORMDB
base64 -i wallet.zip | pbcopy
```

---

## Migración tras actualizar el repo

Si ya tenías wallet local, **se conserva** en tu máquina; git dejará de trackearla.

Tras `git pull`:

1. Confirma que `Wallet_ENROLLMENTPLATFORMDB/` sigue en tu disco (no en git).
2. Si hay conflicto en archivos de wallet eliminados del repo: acepta la eliminación en git y conserva tu copia local.
3. Verifica `.env` con tus credenciales y alias TNS.

---

## Referencias

- [README.md](../README.md) — ejecución y endpoints
- [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md) — RabbitMQ y prueba del flujo asíncrono
- [guia-despliegue-ec2.md](guia-despliegue-ec2.md) — CI/CD y secrets
- [wallet.example/README.md](../wallet.example/README.md) — plantilla de wallet
