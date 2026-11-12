# Cuentas y seguridad

Aplicar `database/lab12-seguridad.sql` solo en el host y esquema `admitu_db` propios previamente verificados. No hay contraseñas predeterminadas ni cuentas administrativas públicas. Provisionar la primera cuenta desde el servidor:

```bash
python scripts/seguridad/crear-cuenta.py --defaults-file /ruta/privada/my.cnf --correo CORREO_PROPIO --rol admin
```

El script solicita contraseña por entrada oculta; alternativamente recibe `ADMITU_NUEVA_CLAVE` desde un gestor privado. No pasar claves como argumentos ni subir el archivo MySQL. Evaluadores y postulantes requieren `--persona-id` de la persona existente del rol; el procedimiento valida que exista. El registro público siempre crea postulantes y guarda persona+cuenta en una sola transacción.

Hash PBKDF2-HMAC-SHA256 con 600000 iteraciones, sal aleatoria16bytes y derivado256bits. Token opaco32bytes, en la base solo SHA256, expiración8horas y revocación al salir. Cinco intentos fallidos bloquean la cuenta5minutos. Cookies HttpOnly/SameSiteStrict con antiforgery; roles del servidor, sin selector de perfil. HttpClient, cliente y DatosSesion tienen alcance scoped. El estado se inicializa desde las claims del circuito autenticado y los listados personales se filtran por propietario en Java.

Las validaciones de pagos/documentos/estados están reservadas a evaluadores/administración. El postulante usa exclusivamente los recursos de archivo con comprobación de propietario y el envío compuesto; no puede aprobar sus propios pagos mediante el REST heredado.

## TLS JDBC

DBManager devuelve una conexión por llamada; no guarda conexión mutable compartida. Configuración por `ADMITU_DB_PROPERTIES` externo, `db.properties` privado en servidor o variables `ADMITU_DB_HOST/PORT/NAME/USER/PASSWORD`. El host remoto exige por defecto `VERIFY_IDENTITY`; para un servidor con CA privada configurar `ADMITU_DB_TRUSTSTORE_URL/PASSWORD/TYPE`, equivalentes a `db.trustStoreUrl/Password/Type`. El truststore PKCS12 debe contener la CA del servidor autorizado. `fallbackToSystemKeyStore=false` evita que MySQL trate el keystore HTTPS de GlassFish como certificado de cliente. No desactivar validación TLS para corregir un fallo de certificados.

En ensayo local con certificado MySQL autogenerado sin nombre DNS, usar `db.sslMode=VERIFY_CA` y confiar expresamente en su CA privada. Producción requiere HTTPS para Blazor/REST y `VERIFY_IDENTITY` hacia MySQL con certificado que coincida con el nombre configurado.
