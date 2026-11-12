#!/usr/bin/env bash
set -euo pipefail
# Compilar primero mvn -B clean package. Solo lee MySQL. No imprime configuración.
repo="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo"
export XDG_CACHE_HOME="$repo/work/cache"
mkdir -p "$XDG_CACHE_HOME"
: "${ADMITU_DB_PROPERTIES:?Defina ADMITU_DB_PROPERTIES con la ruta privada de configuración MySQL}"
test -f "$ADMITU_DB_PROPERTIES"
war=backend/AdmitU/admitu_services/target/admitu_services.war
test -f "$war" || { echo 'Falta WAR; ejecute Maven package.' >&2; exit 1; }
mkdir -p work/reportes-runtime work/reportes-test-classes work/evidencias/reportes
python - <<'PY'
from pathlib import Path
import zipfile
with zipfile.ZipFile('backend/AdmitU/admitu_services/target/admitu_services.war') as z:
 for n in z.namelist():
  if n.startswith('WEB-INF/lib/') and n.endswith('.jar'):
   Path('work/reportes-runtime',Path(n).name).write_bytes(z.read(n))
PY
javac -encoding UTF-8 -cp 'work/reportes-runtime/*' -d work/reportes-test-classes scripts/pruebas/PruebaReportes.java
java -Djava.awt.headless=true -cp 'work/reportes-test-classes:work/reportes-runtime/*' PruebaReportes work/evidencias/reportes | tee work/evidencias/reportes-java.log
if command -v pdftotext >/dev/null; then
 for file in work/evidencias/reportes/*.pdf; do pdftotext -layout "$file" "${file%.pdf}.txt"; done
 python scripts/pruebas/reportes-contenido.py
else
 echo 'NO PROBADO: contraste de texto PDF requiere pdftotext.'
fi
