# Autenticación, autorización y sesiones — evidencia del 12/11/2026

Se reconstruyó esta vertical después del restablecimiento del entorno; las evidencias de esta página corresponden a ejecuciones nuevas sobre GlassFish, MySQL y Blazor locales. Los resultados anteriores al restablecimiento no se usan como aprobación del código actual.

## Implementación

- Registro público exclusivo de postulantes: persona y cuenta se confirman juntas o se revierten juntas. El rol no se acepta del navegador. Las cuentas administrativas y de evaluador se provisionan mediante un procedimiento y un script ejecutados por el responsable.
- PBKDF2-HMAC-SHA256 con 600000 iteraciones y sal aleatoria de 16 bytes; no MD5. La contraseña no aparece en URL, repositorio ni logs de las pruebas. Token opaco de 32 bytes, guardado en MySQL solo como SHA256 y con vencimiento de 8 horas. Cinco intentos fallidos bloquean el inicio de sesión durante 5 minutos.
- Cookie Blazor HttpOnly y SameSite Strict, antiforgery en ingreso y salida, roles en rutas y comprobación de rol/propietario en REST. El cliente no puede aprobar sus pagos, validar documentos, cambiar estados de evaluación ni consultar datos de otro postulante.
- HttpClient, AdmitURestClient y DatosSesion son scoped. Las claims del AuthenticationStateProvider identifican al usuario del circuito; ya no se elige el primer postulante/evaluador como sustituto. Los listados personales se filtran en el servidor Java.
- DBManager conserva configuración inmutable y devuelve una conexión nueva por operación. El modo TLS remoto por defecto es VERIFY_IDENTITY; el ensayo local se ejecutó con VERIFY_CA y un truststore privado que contiene la CA de ese MySQL. No se usa el keystore HTTPS de GlassFish como certificado de cliente MySQL.
- Si el API no responde, el login conserva su formulario y muestra un aviso; el portal protege las pantallas que requieren datos de perfil. Las colecciones de demostración se vacían antes de cargar, sin sustituir fallos por información ficticia.

## Pruebas realmente ejecutadas

| Prueba | Resultado | Evidencia local privada |
|---|---|---|
| Compilación Java de seis módulos | PASS | `work/seguridad-java.log` |
| Compilación Blazor | PASS, 0 errores y 0 advertencias | `work/seguridad-dotnet.log` |
| Hash verifica; otra clave rechazada; sales distintas; token de 43 caracteres | PASS | `work/seguridad-crypto.log` |
| API: autenticación, 401 anónimo, 403 por rol, ownership, bloqueo y revocación | 17/17 PASS | `work/seguridad-api.json` y `.log` |
| Chromium: CSRF, login admin/evaluador, cookies, aislamiento y salida | 6/6 PASS | `work/seguridad-ui.json` y `.log` |
| API deliberadamente inaccesible en instancia Blazor aislada | PASS: HTTP200, aviso visible y formulario de login disponible | `work/seguridad-offline.json` |

Las pruebas de navegador utilizaron dos contextos independientes de Chromium. El evaluador fue rechazado al abrir el área administrativa y vio la pantalla de acceso restringido. Al cerrar sesión se eliminó la cookie; la prueba API verificó que el token revocado devuelve 401. Los procesos temporales de ensayo se detuvieron; el proceso principal de la aplicación se conservó.

Las pruebas usan cuentas y documentos ficticios creados en la base local verificada. El archivo con contraseñas y los logs brutos permanecen fuera de Git; no publicar cookies, tokens, respuestas personales ni el archivo de conexión.

## Reproducción

Provisionar primero cuentas propias siguiendo `scripts/seguridad/README.md`. Crear un archivo JSON privado de credenciales con objetos `admin` y `evaluador`, cada uno con propiedades `correo` y `clave`. Con el backend y el frontend ya iniciados:

```bash
python scripts/pruebas/seguridad.py --credentials-file /ruta/privada/credenciales.json
# El comando anterior solo describe la prueba. --apply crea cuentas ficticias de postulante:
python scripts/pruebas/seguridad.py --credentials-file /ruta/privada/credenciales.json --apply
python scripts/pruebas/seguridad-ui.py --credentials-file /ruta/privada/credenciales.json
```

La prueba UI requiere Playwright Python y Chromium en `/usr/bin/chromium`; las herramientas del curso siguen siendo Java/Jakarta/MySQL y Blazor. Playwright se utiliza exclusivamente para verificar el navegador, sin formar parte de la aplicación. Los scripts no imprimen valores de contraseñas, cookies ni tokens.

La prueba de caída del API arrancó una instancia Blazor temporal apuntando a un puerto local cerrado; no se detuvo ni reconfiguró la base o el servidor principal para provocarla. HTTPS público, IIS y AWS permanecen sujetos a la validación del despliegue; estas pruebas locales no certifican un servidor remoto ni 500 trámites completos simultáneos.
