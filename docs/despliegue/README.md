# Despliegue AdmitU — GlassFish e IIS en EC2 Windows Server

Estado: procedimiento preparado. AWS Academy, Windows Server e IIS no se han ejecutado desde esta sesión. Requieren acceso real al laboratorio autorizado, su cuota/crédito y una instancia existente. No se crean servicios AWS, instancias ni reglas de red con estos scripts. El desarrollo local y sus pruebas se documentan por separado.

## 1. Artefactos y requisitos

- JDK25 y Maven; GlassFish compatible con Java25 y JakartaEE10 (el entorno local utiliza GlassFish8).
- MySQL8 con tablas/SP de AdmitU, cliente mysql y mysqldump; base de ensayo propia `admitu_db` positivamente identificada.
- Windows Server con IIS, .NET10 Hosting Bundle y ASP.NET Core Module, sitio y AppPool existentes. AppPool sin CLR administrado; identidad con lectura de archivos publicados y acceso a sus claves DataProtection.
- Backend y cliente continúan en procesos separados, comunicados por REST; C# no accede a MySQL.

Desde una consola con Java25, Maven y .NET10 en PATH:

```bash
scripts/despliegue/build.sh
```

Produce `backend/AdmitU/admitu_services/target/admitu_services.war` y `work/publish/windows/`. El WAR debe conservarse sin db.properties real. El publish es framework-dependent `win-x64` y requiere el Hosting Bundle de .NET10 en el servidor. El directorio work no se publica en Git.

## 2. MySQL: backup y migraciones

Copiar `scripts/despliegue/my.cnf.example` a una ruta privada fuera del checkout, configurar credenciales y TLS, y restringir permisos. Determinar host autorizado del archivo y el valor real `SELECT @@hostname, DATABASE()` por conexión al servidor de ensayo propio. No usar un host de terceros o producción.

```bash
# Solo describe los archivos y hashes; no abre MySQL.
python scripts/despliegue/migrar.py
# Aplica únicamente tras comprobar ambos destinos y generar dump privado.
python scripts/despliegue/migrar.py --apply --defaults-file /ruta/privada/my.cnf --expected-host HOST_AUTORIZADO --expected-server HOSTNAME_MYSQL_CONFIRMADO
```

Orden: perfil, archivos, reglas, seguridad y reportes. Se especifica UTF8MB4 para preservar tildes. Por defecto no se recrea la BD; `--recreate-test-db` añade el SQL de LAB11 y requiere `--apply`, destino verificado y backup previo. Este modo borra solo admitu_db de ensayo. DDL y procedimientos no se revierten con rollback: conservar el dump y detenerse si una migración falla. El script no elimina duplicados históricos silenciosamente.

Los valores de host/schema no prueban por sí mismos la propiedad del servidor: el operador debe haber confirmado previamente que el destino pertenece a su laboratorio autorizado. No proporcionar contraseñas en argumentos de línea de comandos ni subir archivos de configuración/dumps a Git.

## 3. Conexión backend sin secretos en WAR

Copiar `db.properties.example` fuera del repositorio y completar únicamente su copia privada. Configurar `ADMITU_DB_PROPERTIES` para el proceso GlassFish con la ruta absoluta de ese archivo. También se admiten variables `ADMITU_DB_HOST`, `ADMITU_DB_PORT`, `ADMITU_DB_NAME`, `ADMITU_DB_USER`, `ADMITU_DB_PASSWORD`, `ADMITU_DB_SSL_MODE` y las variables de truststore documentadas por DBManager.

MySQL remoto utiliza `VERIFY_IDENTITY`. Si la CA es privada, importar su certificado CA en un truststore privado y configurar `db.trustStoreUrl`, `db.trustStorePassword`, `db.trustStoreType=PKCS12`. En ensayo loopback con certificado de servidor sin SAN puede utilizarse VERIFY_CA con la CA concreta confiada; no desactivar TLS ni verificación para resolver errores. DBManager establece fallbackToSystemKeyStore=false para evitar que Connector/J intente usar el certificado HTTPS de GlassFish como certificado cliente de MySQL.

Inicio local del dominio existente en una consola PowerShell privada, conservando sus parámetros propios:

```powershell
$env:JAVA_HOME = 'C:\ruta\jdk-25'
$env:ADMITU_DB_PROPERTIES = 'C:\AdmitU-Privado\db.properties'
& 'C:\ruta\glassfish\bin\asadmin.bat' start-domain domain1
```

Si GlassFish ya funciona como servicio Windows, configurar esas variables para su cuenta/proceso y reiniciarlo en su ventana de mantenimiento; las variables de otra consola no cambian un proceso ya iniciado. No introducir claves como opciones JVM públicas ni desactivar comprobaciones TLS.

La cuenta MySQL de aplicación debe acceder solo a admitu_db con privilegios mínimos necesarios para los SP y sus transacciones. La cuenta de migración se usa por separado para DDL. Ajustar la zona horaria MySQL al calendario del proceso de admisión en Lima y comprobarla con `SELECT @@session.time_zone, CURDATE()`.

## 4. Publicar sobre IIS/GlassFish existentes

Configurar previamente `ApiSettings:BaseUrl` para el frontend; en la misma EC2 puede usar `http://127.0.0.1:8080/admitu_services/webresources/`, accesible solo localmente. Si Java está en otro host, usar HTTPS y restringir la conexión a esa red. El navegador se conecta a IIS mediante HTTPS.

Primero ejecutar sin -Apply para inspeccionar el plan:

```powershell
.\scripts\despliegue\publicar-windows.ps1 `
  -GlassFishHome 'C:\ruta\glassfish' `
  -War 'C:\entrega\admitu_services.war' `
  -FrontendPublish 'C:\entrega\publish' `
  -IisPath 'C:\inetpub\AdmitU' `
  -AppPool 'AdmitU' `
  -ApiBaseUrl 'http://127.0.0.1:8080/admitu_services/webresources/'
```

Añadir `-Apply` para desplegar el WAR y copiar el publish después del backup del sitio IIS. El script mantiene el nombre/contexto `admitu_services`, comprueba REST y opcionalmente una `-HealthUrl` de UI. No configura las credenciales del proceso GlassFish, las migraciones, el sitio IIS ni certificados automáticamente. Si falla un paso, conservar logs privados y backups; no declarar despliegue exitoso por una salida parcial.

## 5. Red y cuentas

Mantener 3306 y el puerto REST en loopback/red privada según la topología; no abrir MySQL a Internet. Administración GlassFish4848 solo local/túnel autorizado; RDP3389 restringido a IP administrativa; publicar únicamente IIS443 y, si corresponde, 80 para redirección HTTPS. No crear reglas de acceso amplio como 0.0.0.0/0 para RDP, MySQL o administración.

Crear cuentas con `scripts/seguridad/crear-cuenta.py` según su ayuda, desde una conexión privada autorizada. No existen contraseñas predeterminadas públicas. Entregar credenciales de prueba por canal privado. Comprobar cookies HttpOnly/SameSite y HTTPS, roles en Blazor y REST, aislamiento entre dos navegadores y rechazo al intentar archivos de otro postulante.

## 6. Comprobación posterior y límites de prueba

1. Abrir la URL de IIS y comprobar registro, ingreso, cierre de sesión y acceso por roles.
2. Probar los catálogos y CRUD desde Blazor, incluyendo baja/reanudación con confirmación y borradores de convocatoria.
3. Enviar una postulación con archivos reales; recargar, repetir el envío y lanzar dos POST simultáneos. Contar una postulación y un pago vigente en MySQL.
4. Cargar/descargar DNI, foto, certificado, voucher y archivos requeridos; comprobar mismos bytes después de reiniciar los procesos.
5. Aprobar/rechazar como evaluador; verificar fecha, historial y elegibilidad del carné. Un postulante no puede aprobarse un pago ni un documento.
6. Descargar tres PDF Jasper, comprobar filtros y totales con SQL. El modelo actual no reserva cupos por modalidad; ver docs/lab12/reportes.md.
7. Ejecutar los runners de pruebas y guardar sus logs privados. `carga.py --users 500` mide conexiones GET concurrentes a un endpoint; ese resultado no prueba 500 postulantes completando trámites.

Se comprobó sintaxis de scripts Python/Bash y modo dry-run del migrador en Linux. PowerShell/IIS no se ejecutaron aquí: su prueba corresponde al servidor Windows. El acceso AWS se declarará BLOQUEADO POR ACCESO mientras no exista cuenta/instancia/cuota verificada. Un HTTP200 del catálogo no equivale a prueba E2E ni a publicación en AWS.
