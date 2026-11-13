# Autorización REST de la auditoría preAWS

Esta política se implementa en `AutenticacionFiltro`, `Seguridad` y los recursos REST. Se conservan las rutas consumidas por Blazor. El alcance «Todos los expedientes» permite al evaluador consultar los metadatos de revisión y el reporte Observados; no concede escritura ni descarga de archivos de expedientes sin asignación.

| Recurso/operación | Anónimo | Postulante | Evaluador | Administrador |
|---|---|---|---|---|
| Catálogos GET | Permitido | Permitido | Permitido | Permitido |
| Catálogos POST/PUT/DELETE, reactivación | 401 | 403 | 403 | Permitido según reglas BO/SQL |
| Perfil/antecedente personal | 401 | Solo propio | Consulta de metadatos | Gestión según BO/SQL |
| Postulación GET | 401 | Solo propia | Consulta «Todos» | Consulta |
| Crear postulación genérica | 401 | 403 | 403 | Administración explícita |
| Enviar postulación atómica | 401 | Solo propia | 403 | 403; el envío pertenece al postulante |
| Asignar evaluador | 401 | 403 | 403 | Permitido |
| Revisar estado de postulación | 401 | 403 | Solo asignada | Permitido según transiciones |
| Revisar pago/documento | 401 | 403 | Solo expediente asignado | Permitido según reglas BO/SQL |
| Historial GET | 401 | Solo propio | Consulta | Consulta |
| Historial POST/PUT/DELETE | 401 | 403 | 403 | 403 |
| Crear observación | 401 | 403 | Solo asignada; autor del token | 403; no se firma en nombre de un evaluador |
| Editar texto/tipo de observación | 401 | 403 | Asignada y autor original | Puede editar sin cambiar autor/fecha original |
| Subsanar observación | 401 | Reemplazo de su archivo | Asignada; fecha del servidor | Fecha del servidor |
| Reabrir observación subsanada | 401 | 403 | 403 | 403; registrar otra observación |
| Escribir archivos | 401 | Solo propios, referencia también propia | 403 | Permitido según reglas BO/SQL |
| Listar/descargar DNI, foto o certificado | 401 | Solo propios | Requiere algún expediente del propietario asignado | Permitido |
| Listar/descargar documento o voucher | 401 | Solo propios | Requiere asignación del expediente concreto | Permitido |
| Jasper Ingresos/Vacantes | 401 | 403 | 403 | Permitido |
| Jasper Observados | 401 | 403 | Permitido | Permitido |

Los estados de revisión se aplican sobre la entidad recuperada de la base. El JSON no puede trasladar un pago/documento a otro expediente, modificar su costo ni sustituir sus metadatos. En observaciones se conserva autor, documento y fecha original; una subsanación nueva toma la fecha de Lima en el servidor y las ya registradas conservan su fecha. La creación de observación rechaza un `evaluador.id` distinto del token.

`ActorContext` identifica cada petición autenticada por rol, persona y cuenta, sin correo ni token. El filtro lo limpia al entrar y salir; `DBManager` inicializa las variables de actor en cada conexión. Los procedimientos vuelven a comprobar la asignación bajo bloqueo transaccional antes de actualizar, y generan el historial con el actor del servidor. BO/DAO/endpoints genéricos del historial no permiten mutación. Los procesos locales de laboratorio sin petición se identifican como `SISTEMA`.

La cuenta inactiva, evaluador inactivo, token vencido/revocado y cuenta temporalmente bloqueada no obtienen sesión REST válida. Las pruebas de cookies/CSRF pertenecen además a `seguridad-ui.py` en Chromium.

## Reproducir las pruebas

En una base `admitu_db` de ensayo local identificada y respaldada, preparar un JSON **privado** con `admin`, `evaluador`, `evaluador2`, con `correo`, `clave` y `personaId` de cada evaluador. No publicar ese archivo ni el fichero MySQL de conexión. Ejecutar:

```bash
python3 scripts/pruebas/auditoria-seguridad.py --apply \
  --credentials-file /ruta/privada/credenciales.json \
  --mysql-defaults-file /ruta/privada/mysql.cnf \
  --output work/evidencias/auditoria-seguridad.json
python3 scripts/pruebas/seguridad-ui.py \
  --credentials-file /ruta/privada/credenciales.json \
  --output work/evidencias/seguridad-ui.json
```

La suite REST crea dos postulantes nuevos, usa dos evaluadores diferentes y un administrador, asigna/reasigna expedientes y compara una huella SHA-256 de todas las columnas de las tablas de negocio antes y después de cada rechazo. El SQL devuelve huellas por fila, nunca bytes o datos personales. No ejecutar otras mutaciones sobre esas tablas durante los negativos para que el contraste sea atribuible. Los fixtures se conservan para inspección local. Ninguna ejecución aquí acredita WSL del usuario ni AWS.
