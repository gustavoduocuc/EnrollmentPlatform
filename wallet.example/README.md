# Plantilla de wallet Oracle

La wallet **no se versiona en git**. Cada desarrollador o fork debe descargar la suya desde Oracle Cloud.

## Pasos

1. En **OCI Console** → Autonomous Database → tu instancia → **Database connection** → **Download wallet (Instance Wallet)**.
2. Descomprime el zip. Oracle suele crear una carpeta como `Wallet_<NOMBRE_BD>/`.
3. Coloca el contenido en una de estas rutas:
   - **Recomendado:** `Wallet_ENROLLMENTPLATFORMDB/` en la raíz del proyecto (crea la carpeta si no existe).
   - **Alternativa:** cualquier ruta y define `ORACLE_WALLET_DIR` en tu `.env` o al ejecutar scripts.

## Archivos mínimos requeridos

| Archivo | Descripción |
| ------- | ----------- |
| `tnsnames.ora` | Alias TNS (generado por Oracle al descargar) |
| `sqlnet.ora` | Configuración SSL/wallet |
| `cwallet.sso` o `ewallet.pem` | Certificados mTLS |

Opcional: `ojdbc.properties` (puedes copiar el de esta carpeta `wallet.example/`).

## Alias TNS y JDBC

Abre tu `tnsnames.ora` y busca los alias disponibles:

```bash
grep '^[a-z]' Wallet_ENROLLMENTPLATFORMDB/tnsnames.ora
```

En `.env`, `SPRING_DATASOURCE_URL` debe usar **tu** alias, por ejemplo:

```bash
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<tu_alias>_high
```

## Plantillas de referencia

- `sqlnet.ora` y `ojdbc.properties` en esta carpeta son genéricos.
- `tnsnames.ora.example` solo documenta el formato; **no** sustituye al archivo descargado de OCI.

Ver también: [docs/configuracion-desarrollador.md](../docs/configuracion-desarrollador.md).
