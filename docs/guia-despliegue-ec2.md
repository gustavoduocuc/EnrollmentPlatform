# Guía de despliegue: GitHub Actions → Docker Hub → EC2

Despliegue mediante [`.github/workflows/docker-deploy.yml`](../.github/workflows/docker-deploy.yml).

Cada **fork** configura sus propios secrets en GitHub y su propia instancia EC2. La wallet y el `.env` local no se comparten entre desarrolladores.

## Requisitos previos

- Aplicación operativa en local con `run-prod.sh` o Docker.
- Wallet Oracle descargada de OCI en `Wallet_ENROLLMENTPLATFORMDB/` (local; no está en git).
- Credenciales Oracle en `.env` (mismos valores que se usarán en los secrets de **tu** fork).

La wallet y el `.env` no se copian al servidor EC2. La wallet se incluye en la imagen durante el build en CI (desde el secret `ORACLE_WALLET_BASE64`); usuario y contraseña de Oracle se inyectan en el contenedor desde secrets de GitHub.

---

## 1. Secrets en GitHub

Configurar en **Settings → Secrets and variables → Actions**:

| Secret | Valor |
| ------ | ----- |
| `ORACLE_WALLET_BASE64` | Archivo zip de `Wallet_ENROLLMENTPLATFORMDB` codificado en base64 |
| `SPRING_DATASOURCE_USERNAME` | Usuario Oracle (equivalente a `.env` local) |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña Oracle (equivalente a `.env` local) |
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | Access token de Docker Hub (no la contraseña de la cuenta) |
| `EC2_HOST` | IP pública de la instancia EC2 |
| `USER_SERVER` | Usuario SSH: `ubuntu` (Ubuntu) o `ec2-user` (Amazon Linux) |
| `EC2_SSH_KEY` | Contenido del archivo `.pem` (líneas `BEGIN` a `END` inclusive) |
| `AWS_ACCESS_KEY_ID` | Access key IAM con permisos S3 sobre el bucket de resúmenes |
| `AWS_SECRET_ACCESS_KEY` | Secret key IAM |
| `AWS_SESSION_TOKEN` | Credenciales STS temporales; omitir si no aplica |
| `AWS_REGION` | Región del bucket S3 (ej. `us-east-1`) |
| `AWS_S3_BUCKET` | Bucket donde se guardan los resúmenes (`{enrollmentId}/summary.json`) |
| `SPRING_DATASOURCE_URL` | Opcional. Por defecto: `jdbc:oracle:thin:@enrollmentplatformdb_high` |
| `ENROLLMENT_SECURITY_JWT_ENABLED` | Opcional. `true` activa JWT en Spring; default: `false` |
| `AZURE_B2C_JWK_SET_URI` | Requerido si JWT Spring activo. URL JWK set de Azure AD B2C |
| `AZURE_B2C_AUDIENCE` | Requerido si JWT Spring activo. Client ID de la app en Azure |
| `ENROLLMENT_SECURITY_LOG_LEVEL` | Opcional. Nivel de log de Spring Security; default: `INFO` |
| `SPRING_RABBITMQ_HOST` | IP privada de EC2 donde corre RabbitMQ (ej. `172.31.x.x`). Ver [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md) |

Detalle de las capas de seguridad (Gateway vs Spring): [seguridad-jwt.md](seguridad-jwt.md).

### RabbitMQ en EC2

El pipeline **no** levanta RabbitMQ. Debe estar corriendo en la instancia antes del deploy:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Obtener la IP privada de la instancia (valor típico de `SPRING_RABBITMQ_HOST`):

```bash
curl -s http://169.254.169.254/latest/meta-data/local-ipv4
```

La app en Docker se conecta a RabbitMQ usando esa IP. Guía completa: [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md).

### S3 en producción

1. Crear el bucket en la misma región que `AWS_REGION`.
2. Asignar al IAM de las credenciales permisos `s3:PutObject`, `GetObject`, `DeleteObject` y `ListBucket` sobre ese bucket.
3. Configurar los secrets `AWS_S3_BUCKET` y `AWS_REGION` en GitHub Actions.

Política de ejemplo y formato del resumen: [almacenamiento-s3-resumenes.md](almacenamiento-s3-resumenes.md).

No definir `AWS_S3_ENDPOINT` en producción; el SDK usa el endpoint regional de AWS.

### Generar `ORACLE_WALLET_BASE64`

Usa la wallet de **tu** instancia Oracle (carpeta local `Wallet_ENROLLMENTPLATFORMDB/`):

```bash
cd /ruta/a/EnrollmentPlatform
zip -r wallet.zip Wallet_ENROLLMENTPLATFORMDB
base64 -i wallet.zip | pbcopy
```

Asignar el resultado al secret `ORACLE_WALLET_BASE64` en **tu** fork.

**Si cambias de instancia Oracle** (nueva BD, otro fork): descarga la wallet nueva, regenera el zip en base64 y actualiza el secret. También actualiza `SPRING_DATASOURCE_URL` con el alias de tu nuevo `tnsnames.ora`.

### Determinar `USER_SERVER`

Consola AWS → EC2 → **Connect**, o verificación por SSH:

```bash
ssh -i ruta/a/tu-key.pem ubuntu@TU_IP_EC2
# Alternativa (Amazon Linux):
ssh -i ruta/a/tu-key.pem ec2-user@TU_IP_EC2
```

El usuario válido en el comando SSH es el valor de `USER_SERVER`.

### Configurar `EC2_SSH_KEY`

```bash
cat ruta/a/tu-key.pem
```

Copiar la salida completa al secret. No versionar el archivo `.pem` en el repositorio.

---

## 2. Configuración de EC2

- Docker instalado.
- Security group: regla de entrada TCP en el puerto **8080**.
- Acceso SSH con la llave asociada a `EC2_SSH_KEY`.
- No desplegar `Wallet_ENROLLMENTPLATFORMDB/` ni `.env` en el servidor.
- Contenedor de RabbitMQ corriendo (ver sección [RabbitMQ en EC2](#rabbitmq-en-ec2) arriba).
- Security group: regla de entrada TCP en el puerto **8080** para la aplicación y **15672** (opcional) para la consola web de RabbitMQ.

---

## 3. Ejecutar el despliegue

1. Publicar el código en GitHub (incluye el workflow).
2. Push o merge a la rama `main`.

| Evento | Pipeline |
| ------ | -------- |
| Pull request → `main` | Solo `./mvnw test` |
| Push → `main` | Tests, build, push a Docker Hub, deploy por SSH |

Secuencia del job `build-and-deploy`:

1. `./mvnw test`
2. Build de imagen Docker (wallet desde `ORACLE_WALLET_BASE64`)
3. Push a `{DOCKERHUB_USERNAME}/enrollment-platform:latest`
4. SSH a EC2: `docker pull`, recreación del contenedor con variables Oracle y mapeo interno de RabbitMQ mediante `SPRING_RABBITMQ_HOST`.

---

## 4. Verificación

Sustituir `<IP_EC2>` por la IP pública de la instancia:

```text
http://<IP_EC2>:8080/actuator/health          → 200 (sin token)
http://<IP_EC2>:8080/courses                  → 401 (sin token, JWT Spring activo)
```

Con token Azure vía API Gateway:

```text
https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/courses
Authorization: Bearer <access_token>
→ 200
```

Errores de despliegue: revisar el job `build-and-deploy` en **Actions** del repositorio.

### Verificar procesamiento asíncrono (RabbitMQ)

Tras crear una inscripción en EC2:

```bash
curl -X POST http://<IP_EC2>:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentId": "s-001", "courseIds": ["c-001", "c-002"]}'
```

En Oracle (SQL Developer / Database Actions):

```sql
SELECT * FROM enrollment_summary_mq ORDER BY created_at DESC;
```

Debe aparecer un registro con `status = 'CREATED'`. Más detalle: [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md).

---

## Resumen

| Entorno | Wallet | Credenciales Oracle | Dependencias Externas |
| --- | --- | --- | --- |
| Local | `Wallet_ENROLLMENTPLATFORMDB/` | `.env` | `.env` + LocalStack + RabbitMQ |
| GitHub | `ORACLE_WALLET_BASE64` | `SPRING_DATASOURCE_*` | `AWS_S3_*`, `SPRING_RABBITMQ_HOST` |
| EC2 | Imagen Docker (`/app/wallet`) | Variables en `docker run` (workflow) | S3 + contenedor RabbitMQ manual |

---

## Docker en local (referencia)

```bash
cp .env.docker.example .env
# Configurar usuario y contraseña Oracle

docker build -t enrollment-platform .
docker run -d --name enrollment-platform -p 8080:8080 --env-file .env enrollment-platform
curl http://localhost:8080/actuator/health
```

Ver también [README.md](../README.md) (sección «Producción con Docker»), [configuracion-desarrollador.md](configuracion-desarrollador.md) (setup local) y [procesamiento-asincrono-mq.md](procesamiento-asincrono-mq.md) (RabbitMQ).
