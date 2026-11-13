# Evidencias de ejecución LAB12

Las comprobaciones siguientes corresponden al código reconstruido y a ejecuciones locales reales. Las credenciales, tokens, cookies, dumps, PDFs con datos y capturas permanecen en `work/`, fuera de Git. Solo se versionan resúmenes sanitizados sin datos personales ni secretos.

## Resultados comprobados

| Grupo | Resultado observado | Evidencia reproducible |
|---|---|---|
| Java | Maven completó los seis módulos y produjo el WAR | `mvn -B -f backend/AdmitU/pom.xml clean package`; log privado `work/final-maven.log` |
| Blazor | .NET10 build, 0 errores/advertencias en la ejecución registrada | `dotnet build frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj`; `work/final-dotnet.log` |
| Regresión API | 24/24 PASS, con casos de los 15 RF | [JSON sanitizado](resultados/regresion-api.json); `scripts/pruebas/regresion.py --apply` |
| Reglas/persistencia | 32/32 BO→DAO→SP→MySQL; fixtures propias retiradas en finally | `scripts/pruebas/reglas.sh`; `work/fechas-lima-integracion.log`; [detalle](archivos-reglas.md) |
| Autenticación/roles API | 17/17 PASS | [JSON sanitizado](resultados/seguridad-api.json); `scripts/pruebas/seguridad.py` |
| Cookies y aislamiento | 6/6 Chromium PASS | [JSON sanitizado](resultados/seguridad-ui.json); `scripts/pruebas/seguridad-ui.py` |
| Caída del API | HTTP200 en login, aviso y formulario disponibles; sin datos demo | `work/seguridad-offline.json`; [seguridad](seguridad.md) |
| Jasper/SQL | Tres PDF auténticos; totales, filtros, vacío y negativos contrastados | `scripts/pruebas/reportes.sh`; [detalle](reportes.md) |
| Roles de reportes | 9/9 combinaciones con HTTP esperado | [JSON sanitizado](resultados/reportes-roles.json) |
| Navegación funcional | 15/15 flujos principales PASS | [JSON sanitizado](resultados/ui.json); `scripts/pruebas/ui.py` y capturas privadas |
| Reinicio y bytes | Mismo DNI, 68 bytes y SHA-256 idéntico tras reiniciar GlassFish/frontend | [JSON](resultados/reinicio-archivos.json) |
| O2 y O6 visual | Alineación escritorio/móvil y selección Mis/Todos con dataset distinto | [JSON](resultados/observaciones-alineacion.json) |
| Publicación Windows | dotnet publish Release win-x64 framework-dependent PASS; web.config generado | Log privado work/publish-windows.log; ejecución IIS pendiente |
| Catálogo/carga | 1 y 20 conexiones <3s; 500/500 HTTP200 con p95 8.308s | [1](resultados/catalogo1.json), [20](resultados/catalogo20.json), [500](resultados/carga500.json) |

## Qué se observó en las operaciones críticas

- El envío sin un documento obligatorio se rechazó y dejó cero postulaciones y pagos para ese fixture; dos envíos simultáneos produjeron un éxito y un conflicto, un expediente y un pago. Una carrera y código distintos en la misma convocatoria también se rechazaron; otro postulante o una convocatoria diferente sí se admitieron.
- DNI/foto/certificado/voucher se recuperaron como bytes; el SHA-256 y la comparación con el original comprobaron el contenido. Recargar Blazor y usar otra instancia BO no depende de nombres guardados en memoria. También se recuperó el mismo archivo después de reiniciar completamente el dominio GlassFish y el frontend, con SHA-256 idéntico.
- Aprobar un pago requiere su archivo real; un aprobado no puede modificarse ni perder el voucher por un retiro concurrente. Un documento sin archivo activo no puede aprobarse. La emisión de carné exige aptitud y pago, completa el expediente en la misma transacción y su anulación conserva la fila.
- Los reportes devolvieron HTTP200 con application/pdf y encabezado %PDF-. La extracción con pdftotext concordó con consultas SQL independientes. Los filtros invertidos/negativos se rechazaron y el filtro vacío produjo ceros.
- Un postulante obtuvo 403 al intentar aprobar pagos, validar documentos, cambiar estados de evaluación, editar datos ajenos y abrir reportes administrativos. Sin token los recursos privados devolvieron 401; salir revocó el token.

## Reproducción mínima

Preparar la base de ensayo propia y las cuentas siguiendo [despliegue](../despliegue/README.md) y [provisión de cuentas](../../scripts/seguridad/README.md). Mantener las credenciales fuera del checkout y verificar el destino antes de habilitar escrituras.

```bash
mvn -B -f backend/AdmitU/pom.xml clean package
dotnet build frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj
python scripts/pruebas/regresion.py
# El anterior es dry-run; la ejecución escribe fixtures sintéticos:
python scripts/pruebas/regresion.py --apply --credentials-file /ruta/privada/credenciales.json
python scripts/pruebas/seguridad.py --credentials-file /ruta/privada/credenciales.json --apply
python scripts/pruebas/seguridad-ui.py --credentials-file /ruta/privada/credenciales.json
python scripts/pruebas/ui.py --apply --credentials-file /ruta/privada/credenciales.json
```

La prueba UI requiere los resultados de regresión local en `work/evidencias/regresion.json`, Playwright Python y Chromium. Los runners de navegador usan cuentas y archivos sintéticos. La prueba BO es opt-in y exige `ADMITU_DB_PROPERTIES`, `ADMITU_PRUEBAS_ESCRITURA=true` y el JAR JDBC según `scripts/pruebas/reglas.sh`. No se ejecuta ese main automáticamente al compilar.

## Pruebas no ejecutadas o de alcance limitado

AWS Academy, Windows Server/IIS, ejecución PowerShell de la publicación, HTTPS público y E2E en un servidor remoto: **BLOQUEADO POR ACCESO / NO PROBADO**. La prueba de 500 lecturas públicas no prueba 500 usuarios completando trámites. La preparación de scripts no constituye un despliegue. Los valores concretos de reportes corresponden al instante de cada prueba y pueden cambiar al crear nuevos fixtures.
