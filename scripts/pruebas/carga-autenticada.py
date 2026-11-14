#!/usr/bin/env python3
"""Carga de lecturas privadas local: cinco identidades, sin registrar secretos ni filas.
Con --apply crea dos postulantes sintéticos; no crea/modifica expedientes ni archivos.
"""
import argparse
from collections import Counter
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
import json
import math
from pathlib import Path
import secrets
import statistics
import time
from urllib.parse import urlsplit
from regresion import API


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--apply', action='store_true')
    p.add_argument('--base-url', default='http://127.0.0.1:8080/admitu_services/webresources/')
    p.add_argument('--credentials-file', type=Path, required=True)
    p.add_argument('--requests', type=int, default=100)
    p.add_argument('--output', type=Path, default=Path('work/evidencias/carga-autenticada.json'))
    a = p.parse_args()
    if urlsplit(a.base_url).hostname not in ('localhost', '127.0.0.1', '::1'):
        p.error('Solo se permite el backend local propio de ensayo')
    if not 30 <= a.requests <= 500:
        p.error('--requests debe estar entre 30 y 500 por escenario')
    if not a.apply:
        print('DRY-RUN: use --apply en la instancia Cloud local de ensayo respaldada.')
        return 0
    api = API(a.base_url)
    cred = json.loads(a.credentials_file.read_text())
    identidades = []
    escenarios = []
    try:
        for rol in ('admin', 'evaluador', 'evaluador2'):
            identidades.append((rol, api.login(cred[rol])))
        # Cuentas exclusivas de esta medición; no se modifica el archivo compartido
        # de credenciales ni las identidades de las suites de seguridad y UI.
        for numero in (1, 2):
            tag = secrets.token_hex(6)
            cuenta = dict(correo=f'carga-{tag}@example.test', clave=secrets.token_urlsafe(24))
            persona = dict(nombres='Carga', apellidoPaterno='Sintetica', apellidoMaterno='Ensayo',
                           correo=cuenta['correo'], tipoDocumento='DNI',
                           numeroDocumento=str(20000000 + secrets.randbelow(60000000)),
                           fechaNacimiento='2000-01-01')
            api.json('AutenticacionRS/registrar', 'POST', dict(postulante=persona, clave=cuenta['clave']))
            identidades.append((f'postulante{numero}', api.login(cuenta)))
        rutas = ('PostulacionRS/listarTodasPostulaciones', 'PagoRS/listarTodosPagos',
                 'DocumentoPostulacionRS/listarTodosDocumentosPostulacion')
        for concurrencia in (1, 5, 10):
            def peticion(indice):
                perfil, token = identidades[indice % len(identidades)]
                ruta = rutas[(indice // len(identidades)) % len(rutas)]
                inicio = time.perf_counter()
                try:
                    codigo, _, contenido = api.call(ruta, token=token)
                    # Verificar JSON evita contar como éxito una página de error HTML.
                    if codigo == 200 and not isinstance(json.loads(contenido), list):
                        codigo = -1
                    return perfil, ruta, codigo, time.perf_counter() - inicio
                except Exception:
                    return perfil, ruta, 0, time.perf_counter() - inicio
            inicio = time.perf_counter()
            with ThreadPoolExecutor(max_workers=concurrencia) as executor:
                filas = list(executor.map(peticion, range(a.requests)))
            duracion = time.perf_counter() - inicio
            latencias = sorted(f[3] for f in filas)
            percentil = lambda q: round(latencias[max(0, math.ceil(len(latencias) * q) - 1)], 6)
            escenarios.append(dict(
                concurrencia_solicitudes=concurrencia, identidades_distintas=5,
                solicitudes=len(filas), segundos=round(duracion, 6),
                http=dict(Counter(str(f[2]) for f in filas)),
                fallos=sum(f[2] != 200 for f in filas),
                solicitudes_por_perfil=dict(Counter(f[0] for f in filas)),
                solicitudes_por_ruta=dict(Counter(f[1] for f in filas)),
                latencia_segundos=dict(mediana=round(statistics.median(latencias), 6),
                                      p95=percentil(.95), p99=percentil(.99), maximo=round(max(latencias), 6))))
    finally:
        for _, token in identidades:
            try:
                api.call('AutenticacionRS/salir', 'POST', token=token)
            except Exception:
                pass
        informe = dict(
            entorno='Codex Cloud propio; GlassFish/MySQL locales; no WSL ni AWS',
            fecha_utc=datetime.now(timezone.utc).isoformat(),
            preparacion='1 administrador, 2 evaluadores y 2 postulantes sintéticos; login y registro fuera de las latencias',
            modificaciones='Solo creación de dos cuentas sintéticas y sesiones; sin escritura de expedientes/pagos/documentos',
            limite='Lecturas privadas sobre datos de ensayo; dos postulantes nuevos sin expediente. Diez solicitudes pueden compartir cinco identidades. No prueba archivos, escrituras, navegador, 500 usuarios ni cumplimiento RNF04.',
            escenarios=escenarios)
        a.output.parent.mkdir(parents=True, exist_ok=True)
        a.output.write_text(json.dumps(informe, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(informe, ensure_ascii=False, indent=2))
    return 0 if len(escenarios) == 3 and all(e['fallos'] == 0 for e in escenarios) else 1


if __name__ == '__main__':
    raise SystemExit(main())
