#!/usr/bin/env python3
"""Restaurar un dump propio de admitu_db con hash y destino verificados. Dry-run por defecto."""
import argparse
import configparser
from datetime import datetime, timezone
import hashlib
import os
from pathlib import Path
import re
import shutil
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
    p.add_argument('--backup-dir', type=Path, default=ROOT/'work/backups')
    p.add_argument('--apply', action='store_true')
    a = p.parse_args()
    if not a.backup_file.is_file() or a.backup_file.suffix != '.sql':
        p.error('Se requiere un dump .sql completo propio; no se aceptan archivos .partial.')
    contenido = a.backup_file.read_bytes()
    digest = hashlib.sha256(contenido).hexdigest()
    if not contenido or not re.fullmatch('[0-9a-fA-F]{64}', a.sha256) or digest != a.sha256.lower():
        p.error('El dump está vacío o su SHA256 no coincide; no se abrió MySQL.')
    for m in re.finditer(r'\b(?:USE|CREATE\s+DATABASE(?:\s+IF\s+NOT\s+EXISTS)?|DROP\s+DATABASE(?:\s+IF\s+EXISTS)?)\s+`?([\w]+)', contenido.decode('utf-8-sig'), re.I):
        if m.group(1).lower() != 'admitu_db':
            p.error('El dump menciona otro esquema; no se abrió MySQL.')
    print('PLAN: verificar destino admitu_db, respaldar su estado actual y restaurar dump SHA256='+digest)
    if not a.apply:
        print('DRY-RUN: no se conectó a MySQL ni se modificó la base.')
        return
    if not a.defaults_file or not a.defaults_file.is_file() or not a.expected_host or not a.expected_server:
        p.error('--apply exige --defaults-file, --expected-host y --expected-server')
    cnf = configparser.ConfigParser(interpolation=None, allow_no_value=True)
    cnf.read(a.defaults_file, encoding='utf-8')
    if cnf.get('client', 'host', fallback='localhost').strip('\"\'') != a.expected_host:
        p.error('Host configurado distinto al autorizado')
    if not shutil.which('mysql') or not shutil.which('mysqldump'):
        p.error('mysql y mysqldump deben estar en PATH')
    flags = ['--defaults-extra-file='+str(a.defaults_file.resolve()), '--default-character-set=utf8mb4']
    try:
        r = subprocess.run(['mysql', *flags, '-N', 'admitu_db', '-e', 'SELECT @@hostname,DATABASE()'], capture_output=True, text=True, check=True)
        if r.stdout.strip().split('\t') != [a.expected_server, 'admitu_db']:
            p.error('Servidor o esquema no coincide con el destino de ensayo verificado')
        a.backup_dir.mkdir(parents=True, exist_ok=True)
        respaldo = a.backup_dir/('antes-restaurar-'+datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%S%fZ')+'.sql')
        parcial = respaldo.with_suffix('.sql.partial')
        with parcial.open('xb') as out:
            os.chmod(parcial, 0o600)
            subprocess.run(['mysqldump', *flags, '--single-transaction', '--routines', '--triggers', '--no-tablespaces', '--set-gtid-purged=OFF', 'admitu_db'], stdout=out, stderr=subprocess.PIPE, check=True)
        if not parcial.stat().st_size:
            sys.exit('ERROR: respaldo previo vacío; no se restauró.')
        parcial.rename(respaldo)
        with a.backup_file.open('rb') as src:
            subprocess.run(['mysql', *flags, 'admitu_db'], stdin=src, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
        print('OK: dump restaurado en admitu_db; comprobar conteos, bytes, SP y flujos antes de reabrir la aplicación.')
    except subprocess.CalledProcessError:
        sys.exit('ERROR MySQL. Restauración detenida; conserve respaldo previo y revise logs privados. DDL no es atómico.')

if __name__ == '__main__':
    main()
