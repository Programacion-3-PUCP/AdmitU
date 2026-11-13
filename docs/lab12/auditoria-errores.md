# Errores y diagnósticos (H15)

Los 122 mensajes de error DAO que imprimían `ex.getMessage()` ahora usan
`Diagnostico.resumen`: tipo de excepción y, si existe causa SQL, SQLState y código
numérico. No se imprimen consultas, valores duplicados, correos, contraseñas,
cookies o tokens contenidos en mensajes de excepciones. El mapper REST conserva
el identificador aleatorio de referencia y registra ese diagnóstico sanitizado;
ya no imprime la cadena completa de excepciones SQL. Las respuestas de conflicto
siguen sin exponer los detalles internos de la base.

Prueba reproducible, usando el código de producción y sin conexión a MySQL:

```bash
mkdir -p work/diagnostico-classes
javac -d work/diagnostico-classes \
  backend/AdmitU/admitu_dbmanager/src/main/java/pe/edu/pucp/admitu/config/Diagnostico.java \
  scripts/pruebas/PruebaDiagnostico.java
java -cp work/diagnostico-classes PruebaDiagnostico
```

Resultado Cloud: **6/6 PASS**. Se comprueba ausencia de datos privados sintéticos,
conservación de SQLState/código, causa SQL anidada, SQLState malicioso, error nulo
y cadena cíclica sin bloqueo. El build Maven integrado también pasa.

La carga inicial tiene estado de espera y tratamiento de errores del servidor.
Las acciones de observación actualizan la memoria después de confirmar la
persistencia y muestran un error si el servidor rechaza la operación. El trabajo
pendiente de convertir todos los CRUD heredados a async pertenece a H06. La
ausencia de horas/fechas históricas se trata en H09, sin fabricar metadatos.

No se certifican accesibilidad completa, todas las conexiones degradadas posibles
ni la operación Windows/IIS. Esos límites permanecen explícitos en el informe.
