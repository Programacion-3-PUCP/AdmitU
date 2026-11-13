# Diagnóstico heredado y trazabilidad

El diagnóstico de LAB11 se conserva en [arquitectura.md](../../arquitectura.md) y [plan-lab12.md](../../plan-lab12.md). Sus afirmaciones históricas no son pruebas de ejecución. Las brechas de partida eran archivos guardados por nombre, tres reportes sin Jasper, autenticación demostrativa, sesiones compartidas y envío sin garantía atómica de unicidad.

La [recuperación](recuperacion.md) registra la pérdida del primer entorno y la reconstrucción. Las continuaciones posteriores recuperaron la rama remota y conservaron sus commits; no repitieron las funcionalidades implementadas. Las pruebas visuales detectaron y permitieron corregir el mapeo de estados SQL/Blazor y la selección de alcance de Observaciones. También se corrigió el calendario UTC/Lima de las fechas de negocio.

El estado comprobado final está en la [matriz de los 15 RF](matriz-rf.md), las [evidencias](evidencias.md) y las [incidencias y límites](incidencias.md). Los resultados históricos del diagnóstico no se cuentan como pruebas aprobadas del código actual.
