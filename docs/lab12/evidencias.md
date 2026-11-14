# Evidencias PRE-AWS — ejecución actual

Corte Cloud13/11/2026, rama `codex/auditoria-preaws`, base `2ad4019`.
Los resultados actuales y los17 hallazgos están en [auditoría](auditoria-preaws.md).
No se accedió a WSL del propietario ni se desplegó AWS.

| Grupo | Resultado actual | Evidencia |
|---|---|---|
| Compilación | Maven Java 25 PASS, Blazor .NET 10 PASS sin errores/advertencias | Comandos y alcance en informe PRE-AWS |
| REST15 RF | 24/24PASS | [JSON](resultados/preaws-regresion-api.json) |
| BO/MySQL | 32 LAB12+44auditoríaPASS | [Integridad](resultados/auditoria-integridad.json), rollback y actor servidor |
| SeguridadREST | 77/77PASS;64 rechazos sin cambios en28 tablas | [JSON](resultados/auditoria-seguridad.json) |
| MétricasKPI | 31/31SQL/API/ChromiumPASS | [JSON](resultados/auditoria-kpi-sql.json) |
| JasperREST | 33/33 PDF/SQL/roles/filtrosPASS | [JSON](resultados/auditoria-reportes.json) |
| Fechas/tarifas | 3fronterasREST/SQLPASS y35pruebas C# | [Fechas](resultados/preaws-fechas-tarifas.json), [C#](resultados/auditoria-carga-sesion.json) |
| Modelo indicadores | 14/14PASS | [JSON](resultados/auditoria-metricas-modelos.json) |
| Backups | 8 tests+restauraciónREAL en clonPASS | [JSON](resultados/auditoria-operaciones.json) |
| Logging | 6/6PASSsin secretos | [JSON](resultados/auditoria-diagnostico.json) |
| Carga autenticada | 300/300 GET,5 identidades; no500 trámites | [JSON](resultados/auditoria-carga-autenticada.json) |
| Catálogo carreras | 1/20 conexiones todas≤3 s | [1](resultados/preaws-catalogo1.json), [20](resultados/preaws-catalogo20.json) |
| Registro real | PASS formulario protegido, cuenta y perfil persistidos | [JSON](resultados/preaws-registro-ui.json) |
| Chromium | 15/15flujos principales; login/CSRF final aún en verificación | [Flujos](resultados/preaws-ui.json), [cookies](resultados/preaws-seguridad-ui.json), [SSR](resultados/preaws-login-hidratacion.json); visor integrado sujeto al límite Cloud |

Los JSON antiguos siguientes son evidencia histórica de LAB12, conservada para
trazabilidad. No sustituyen las ejecuciones PRE-AWS ni invalidan los defectos que
se detectaron posteriormente. PDFs con datos, capturas, claves, cookies, tokens,
configuración y dumps siguen privados en work/, fuera de Git.

## Historial LAB12 anterior


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
| Catálogo/carga | 1 y 20 conexiones <3 s; 500/500 HTTP200 con p95 8.308 s | [1](resultados/catalogo1.json), [20](resultados/catalogo20.json), [500](resultados/carga500.json) |

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
