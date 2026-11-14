#!/usr/bin/env python3
"""Prueba focal PDF: dos códigos NULL no deben ampliar el ámbito de evaluación.

Solo modifica dos fixtures identificados por manifest en un clon loopback
verificado. Restaura sus códigos en finally y nunca publica texto de los PDF.
"""
import argparse
import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import urlsplit

from regresion import API


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply', action='store_true')
    parser.add_argument('--base-url', default='http://127.0.0.1:8080/admitu_services/webresources/')
    parser.add_argument('--credentials-file', type=Path, required=True)
    parser.add_argument('--fixtures-manifest', type=Path, required=True)
    parser.add_argument('--mysql-defaults-file', type=Path, required=True)
    parser.add_argument('--expected-server', required=True)
    parser.add_argument('--output', type=Path, default=Path('work/privacidad/evidencias/privacidad-pdf-identidad.json'))
    args = parser.parse_args()
    if not args.apply:
        print('DRY-RUN: ninguna conexión o escritura; --apply requiere clon propio verificado.')
        return 0
    if urlsplit(args.base_url).hostname not in ('127.0.0.1', 'localhost'):
        parser.error('El API de ensayo debe ser loopback del clon verificado.')

    def sql(query):
        result = subprocess.run(['mysql', '--defaults-extra-file='+str(args.mysql_defaults_file.resolve()),
                                 '--protocol=TCP', '--host=127.0.0.1', '--batch', '--raw',
                                 '--skip-column-names', 'admitu_db'],
                                input=query, text=True, capture_output=True)
        if result.returncode:
            raise RuntimeError('No se pudo consultar el clon verificado')
        return result.stdout

    if sql('SELECT @@hostname,DATABASE();').strip().split('\t') != [args.expected_server, 'admitu_db']:
        parser.error('El cliente SQL no identifica el clon esperado.')
    fixtures = json.loads(args.fixtures_manifest.read_text())['fixtures']
    assert len(fixtures) == 2 and len({int(f['postulacionId']) for f in fixtures}) == 2
    ids = [int(f['postulacionId']) for f in fixtures]
    assert all(identifier > 0 for identifier in ids)
    clause = ','.join(map(str, ids))
    original = {}
    call_ids = set()
    for line in sql('SELECT id,postulante_id,convocatoria_id,codigo_inscripcion FROM postulacion WHERE id IN ('+clause+') ORDER BY id;').splitlines():
        identifier, owner, call_id, code = line.split('\t')
        fixture = next(f for f in fixtures if int(f['postulacionId']) == int(identifier))
        assert int(owner) == int(fixture['personaId']) and code == fixture['codigo']
        assert re.fullmatch(r'[A-Za-z0-9-]{1,30}', code), 'Código no reconocido como fixture sintético'
        original[int(identifier)] = code
        call_ids.add(int(call_id))
    assert len(original) == 2 and len(call_ids) == 1
    call_id = next(iter(call_ids))
    api = API(args.base_url)
    credentials = json.loads(args.credentials_file.read_text())
    tokens = {role: api.login(credentials[role]) for role in ('admin', 'evaluador', 'evaluador2')}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    folder = args.output.parent / (args.output.stem+'-pdf-private')
    folder.mkdir(exist_ok=True)
    results = []
    restored = False
    codes_before = hashlib.sha256(json.dumps(original, sort_keys=True).encode()).hexdigest()
    try:
        sql('START TRANSACTION; UPDATE postulacion SET codigo_inscripcion=NULL WHERE id IN ('+clause+'); COMMIT;')
        assert sql('SELECT COUNT(*) FROM postulacion WHERE id IN ('+clause+') AND codigo_inscripcion IS NULL;').strip() == '2'
        for role, permitted in [('evaluador', {0}), ('evaluador2', {1}), ('admin', {0, 1})]:
            status, headers, data = api.call('ReporteRS/generarObservados?convocatoriaId='+str(call_id), token=tokens[role])
            pdf_ok = status == 200 and headers.get('Content-Type', '').startswith('application/pdf') and data.startswith(b'%PDF-')
            matches = []
            if pdf_ok:
                path = folder / (role+'.pdf')
                path.write_bytes(data)
                text = subprocess.check_output(['pdftotext', str(path), '-'], text=True)
                matches = [f['descripcion'] in text for f in fixtures]
            correct = pdf_ok and matches == [index in permitted for index in range(2)]
            results.append(dict(case='PDF con códigos NULL '+role, status='PASS' if correct else 'FAIL',
                                http=status, pdf_real=pdf_ok, expected_private_rows=len(permitted),
                                actual_private_rows=sum(matches), foreign_text_absent=all(not match for index,match in enumerate(matches) if index not in permitted)))
            print(('PASS' if correct else 'FAIL')+' PDF con códigos NULL '+role, flush=True)
    finally:
        updates = ['UPDATE postulacion SET codigo_inscripcion=\''+code+'\' WHERE id='+str(identifier)+';' for identifier,code in original.items()]
        sql('START TRANSACTION;'+''.join(updates)+'COMMIT;')
        current = {int(row.split('\t')[0]): row.split('\t')[1] for row in sql('SELECT id,codigo_inscripcion FROM postulacion WHERE id IN ('+clause+') ORDER BY id;').splitlines()}
        restored = current == original
        assert restored, 'Los códigos de los fixtures no se restauraron'
        summary = dict(environment='Codex Cloud; clon MySQL propio; no base original/WSL/AWS',
                       cases=results, codes_restored=restored, codes_before_sha256=codes_before,
                       codes_after_sha256=hashlib.sha256(json.dumps(current, sort_keys=True).encode()).hexdigest(),
                       passed=sum(row['status']=='PASS' for row in results), failed=sum(row['status']=='FAIL' for row in results))
        args.output.write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
    return 0 if len(results)==3 and all(row['status']=='PASS' for row in results) and restored else 1


if __name__ == '__main__':
    try:
        raise SystemExit(main())
    except Exception as error:
        print('FAIL prueba PDF por identidad: '+type(error).__name__)
        raise SystemExit(1)
