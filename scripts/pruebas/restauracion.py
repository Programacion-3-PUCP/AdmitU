#!/usr/bin/env python3
"""Ensayo de recuperación sobre un clon propio de admitu_db; dry-run por defecto.

El dump y la configuración son privados. Nunca dirigir a la base usada por la
aplicación: este ensayo restaura dos veces y crea una convocatoria sintética.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import secrets
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--backup-file', type=Path, required=True)
    p.add_argument('--sha256', required=True)
    p.add_argument('--defaults-file', type=Path)
    p.add_argument('--expected-host')
    p.add_argument('--expected-server')
    p.add_argument('--backup-dir', type=Path, default=ROOT/'work/restauracion-previos')
    p.add_argument('--output', type=Path, default=ROOT/'work/evidencias/restauracion.json')
    p.add_argument('--apply', action='store_true')
    a = p.parse_args()
    command = [sys.executable, str(ROOT/'scripts/despliegue/restaurar.py'),
               '--backup-file', str(a.backup_file.resolve()), '--sha256', a.sha256]
    # El restaurador valida el dump/hash también en dry-run; no abre MySQL.
    subprocess.run(command, check=True)
    if not a.apply:
        print('DRY-RUN: el ensayo no abrió MySQL ni alteró el clon.')
        return
    if not a.defaults_file or not a.expected_host or not a.expected_server:
        p.error('--apply exige configuración y destino del clon propio verificado')
    mysql = ['mysql', '--defaults-extra-file='+str(a.defaults_file.resolve()),
             '--default-character-set=utf8mb4', '-N', 'admitu_db']
    def query(sql):
        return subprocess.run([*mysql, '-e', sql], capture_output=True, text=True, check=True).stdout.strip()
    if query('SELECT @@hostname,DATABASE()').split('\t') != [a.expected_server, 'admitu_db']:
        p.error('Servidor/esquema distintos al clon verificado; no se restauró')
    command += ['--apply', '--defaults-file', str(a.defaults_file.resolve()),
                '--expected-host', a.expected_host, '--expected-server', a.expected_server,
                '--backup-dir', str(a.backup_dir.resolve())]
    def fingerprint():
        tables = query("SELECT TABLE_NAME FROM information_schema.tables WHERE table_schema='admitu_db' AND TABLE_TYPE='BASE TABLE' ORDER BY TABLE_NAME").splitlines()
        if not tables or any(not name.replace('_', '').isalnum() for name in tables):
            raise RuntimeError('Inventario de tablas inválido')
        checksum = query('CHECKSUM TABLE '+','.join('`'+name+'`' for name in tables)+' EXTENDED')
        routines = query("SELECT ROUTINE_NAME,ROUTINE_TYPE,SHA2(COALESCE(ROUTINE_DEFINITION,''),256) FROM information_schema.routines WHERE routine_schema='admitu_db' ORDER BY ROUTINE_NAME,ROUTINE_TYPE")
        rows = {name: int(query('SELECT COUNT(*) FROM `'+name+'`')) for name in tables}
        blob = [int(x) for x in query('SELECT COUNT(*),COALESCE(SUM(sha256=LOWER(SHA2(contenido,256))),0),COALESCE(SUM(OCTET_LENGTH(contenido)),0) FROM archivo_digital').split('\t')]
        return dict(tables=len(tables), routines=len(routines.splitlines()), row_counts=rows,
                    table_checksums_sha256=hashlib.sha256(checksum.encode()).hexdigest(),
                    routine_definitions_sha256=hashlib.sha256(routines.encode()).hexdigest(),
                    blobs=dict(count=blob[0], valid_sha256=blob[1], bytes=blob[2]))
    # La primera restauración también respalda antes el estado del clon.
    subprocess.run(command, check=True)
    before = fingerprint()
    tag = 'RT'+secrets.token_hex(5).upper()
    query("INSERT INTO convocatoria(codigo_convocatoria,nombre,periodo,fecha_inicio,fecha_fin,estado,descripcion,activo) SELECT '"+tag+"','Fixture restauración','ENSAYO',fecha_inicio,fecha_fin,'BORRADOR','Clon propio de prueba',1 FROM convocatoria ORDER BY id LIMIT 1")
    changed = fingerprint()
    if changed['table_checksums_sha256'] == before['table_checksums_sha256']:
        raise AssertionError('La fixture no alteró el clon; ensayo inconcluso')
    subprocess.run(command, check=True)
    after = fingerprint()
    if before != after:
        raise AssertionError('La recuperación no restauró tablas/SP/filas/bytes idénticos')
    if after['blobs']['count'] != after['blobs']['valid_sha256']:
        raise AssertionError('SHA256 de algún archivo distinto a sus bytes')
    result = dict(environment='Clon propio de ensayo; no acredita WSL, Windows/IIS ni AWS',
                  passed=True, target_verified=True, synthetic_mutation_verified=True,
                  restored_fingerprint_identical=True, source_dump_sha256=a.sha256.lower(), **after)
    a.output.parent.mkdir(parents=True, exist_ok=True)
    a.output.write_text(json.dumps(result, indent=2)+'\n')
    os.chmod(a.output, 0o600)
    print('PASS: recuperación del clon, tablas/SP/filas/bytes idénticos; evidencia sanitizada guardada.')

if __name__ == '__main__':
    main()
