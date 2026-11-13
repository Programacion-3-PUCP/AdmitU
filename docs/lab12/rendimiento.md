# Medición local de carreras y concurrencia

Se ejecutó `scripts/pruebas/carga.py` contra `CarreraRS/listarTodasCarreras` del WAR nuevo en GlassFish, con MySQL local y conexión JDBC por operación. El runner abre conexiones HTTP simultáneas y cuenta estados/latencias; no simula navegación ni el envío completo de postulaciones. Las solicitudes de esta medición fueron públicas, con cero identidades autenticadas.

| Escenario | Respuestas200 | Mediana | p95 | Máxima |
|---|---|---|---|---|
| 1 conexión | 1/1 | 0.033s | 0.033s | 0.033s |
| 20 conexiones | 20/20 | 0.210s | 0.333s | 0.338s |
| 500 conexiones | 500/500 | 4.978s | 8.308s | 8.657s |

La muestra aislada y la de20 conexiones están por debajo de3 segundos. En500 conexiones, el servidor respondió a todas pero excedió3 segundos. Es evidencia del límite actual de este escenario local, no cumplimiento de RNF04 de500 postulantes completando trámites. No se utilizaron identidades distintas ni sesiones Blazor en esta prueba.

Archivos de resultados privados sin tokens: `work/evidencias/catalogo1.json`, `catalogo20.json`, `carga500.json`. Reproducción:

```bash
python scripts/pruebas/carga.py --users 1 --output work/evidencias/catalogo1.json
python scripts/pruebas/carga.py --users 20 --output work/evidencias/catalogo20.json
python scripts/pruebas/carga.py --users 500 --output work/evidencias/carga500.json
```

Para probar accesos autenticados, el runner admite `--tokens-file` privado con un token por usuario y reporta cuántos tokens distintos recibió. Eso mejora la identidad de la prueba, pero sigue siendo un GET y no un trámite completo. Para certificar RNF04 se requieren500 cuentas/escenarios aislados, tráfico representativo, observación de recursos y verificación de persistencia/atomicidad, en infraestructura de capacidad conocida.
