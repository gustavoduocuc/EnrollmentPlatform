# Procesamiento asíncrono con RabbitMQ

Al crear una inscripción (`POST /enrollments`), la aplicación:

1. Guarda la inscripción en la base de datos.
2. Sube el resumen JSON a S3 (flujo semana 1, síncrono).
3. Publica un mensaje en RabbitMQ con `enrollmentId`, `studentId` y `status`.
4. Un consumidor (`EnrollmentSummaryListener`) procesa el mensaje y persiste un registro en la tabla `enrollment_summary_mq`.

No hay un endpoint HTTP adicional para el consumidor: el procesamiento es vía `@RabbitListener`.

---

## Infraestructura RabbitMQ

| Componente | Valor |
| ---------- | ----- |
| Exchange | `enrollment.exchange` |
| Cola | `enrollment.summary.queue` |
| Routing key | `enrollment.routing.key` |
| Imagen Docker | `rabbitmq:3-management` |
| Puerto AMQP | `5672` |
| Consola web | `15672` (usuario/contraseña por defecto: `guest` / `guest`) |

Variables de entorno (ver `.env.example`):

| Variable | Local nativo | Docker Compose |
| -------- | ------------ | -------------- |
| `RABBITMQ_HOST` | `localhost` | `rabbitmq` |
| `RABBITMQ_PORT` | `5672` | `5672` |
| `RABBITMQ_USER` | `guest` | `guest` |
| `RABBITMQ_PASS` | `guest` | `guest` |

---

## Probar en local (perfil `prod` + Oracle)

**1. Levantar dependencias**

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker compose up -d localstack
docker exec "$(docker ps -qf 'ancestor=localstack/localstack:4.4.0')" \
  awslocal s3 mb s3://enrollment-platform-summaries
```

**2. Configurar `.env`**

Asegúrate de tener Oracle y RabbitMQ:

```bash
SPRING_PROFILES_ACTIVE=prod
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
# ... credenciales Oracle y AWS (LocalStack)
ENROLLMENT_SECURITY_JWT_ENABLED=false
```

**3. Arrancar la app**

```bash
./run-prod.sh
```

**4. Crear inscripción**

```bash
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentId": "s-001", "courseIds": ["c-001", "c-002"]}'
```

**5. Verificar**

- **Logs de la app:** busca `Mensaje enviado a RabbitMQ exitosamente` y `Resumen guardado satisfactoriamente en la nueva tabla de la BD.`
- **Consola RabbitMQ:** http://localhost:15672 → cola `enrollment.summary.queue` (el mensaje debe consumirse).
- **Oracle:** ejecuta en SQL Developer o Database Actions:

```sql
SELECT * FROM enrollment_summary_mq ORDER BY created_at DESC;
```

---

## Probar en local (perfil `local` + H2)

Más simple si no necesitas Oracle:

```bash
docker compose up -d localstack rabbitmq
docker exec "$(docker ps -qf 'ancestor=localstack/localstack:4.4.0')" \
  awslocal s3 mb s3://enrollment-platform-summaries
./run-local.sh
```

Mismo `curl` de inscripción. Verifica en **H2 Console** (http://localhost:8080/h2-console):

- JDBC: `jdbc:h2:mem:enrollment`
- Usuario: `sa`, sin contraseña

```sql
SELECT * FROM enrollment_summary_mq;
```

---

## Probar con Docker Compose

```bash
cp .env.docker.example .env
# Completa credenciales Oracle
docker compose up -d --build
```

En `.env` para Compose, `RABBITMQ_HOST` debe ser `rabbitmq` (no `localhost`).

---

## Despliegue en EC2

RabbitMQ **no** se levanta automáticamente en el pipeline. En la instancia EC2:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Configura el secret de GitHub **`SPRING_RABBITMQ_HOST`** con la IP privada de la instancia EC2 (ej. `172.31.x.x`). La app en Docker se conecta a RabbitMQ vía esa IP.

Obtener IP privada en EC2:

```bash
curl -s http://169.254.169.254/latest/meta-data/local-ipv4
```

Detalle de secrets: [guia-despliegue-ec2.md](guia-despliegue-ec2.md).

---

## Tabla `enrollment_summary_mq`

Creada por Flyway (`V3__create_enrollment_summary_mq_table.sql`):

| Columna | Descripción |
| ------- | ----------- |
| `id` | UUID del registro MQ |
| `enrollment_id` | ID de la inscripción |
| `student_id` | ID del estudiante |
| `status` | Estado del mensaje (ej. `CREATED`) |
| `created_at` | Timestamp de inserción |

---

## Solución de problemas

| Síntoma | Causa probable | Acción |
| ------- | -------------- | ------ |
| Error al publicar en RabbitMQ | RabbitMQ no está levantado | `docker ps` y levantar contenedor |
| `Connection refused` en prod local | `RABBITMQ_HOST` incorrecto | Usar `localhost` (nativo) o `rabbitmq` (Compose) |
| Tabla no existe en Oracle | Flyway V3 no corrió | Revisar logs de arranque; ejecutar migraciones |
| Cola con mensajes sin consumir | Listener no arrancó | Revisar logs; confirmar perfil activo y conexión RabbitMQ |
| Tests CI pasan pero MQ no se prueba | E2E mockea el publisher | Probar manualmente con los pasos anteriores |
