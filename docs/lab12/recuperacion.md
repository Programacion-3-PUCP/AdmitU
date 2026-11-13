# Continuación de LAB12 — 12/11/2026

Al reanudar se encontró el entorno restaurado al snapshot de preparación: HEAD lab11-diego 44851f3, árbol limpio, sin la rama codex/lab12-completo, sin agentes activos ni archivos docs/lab12, scripts de pruebas o código generado previamente. Se consultaron ramas, reflog, objetos inalcanzables y ramas remotas codex/*; no se encontró una copia recuperable.

El commit local previo 6f88586 y los cambios de seis agentes figuraban en el registro de conversación, pero no estaban en este sistema de archivos. El push anterior había sido únicamente --dry-run; no se presenta como una publicación efectiva.

Se conservan los hallazgos, contratos y pruebas descritos en la conversación para reconstruir las mejoras sin repetir el diagnóstico inicial. Las pruebas anteriores no se atribuirán automáticamente al código reconstruido: se repetirán sobre los commits actuales. Se trabaja exclusivamente en codex/lab12-completo desde 44851f3, con tres agentes y commits publicados por etapas. No se modifica lab11-diego ni master.

Decisiones recuperadas: archivos reales en MySQL con BO/DAO/SP, envío compuesto atómico con unicidad postulante+convocatoria y un pago vigente, tres Jasper auténticos, autenticación salada y roles/ownership en servidor, aislamiento de sesión, filtros/confirmaciones UI y despliegue reproducible. El estado final distinguirá API, compilación y E2E visual, y señalará cualquier brecha sin inflar el porcentaje.
