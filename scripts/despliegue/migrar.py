#!/usr/bin/env python3
"""Preflight + backup + migraciones de admitu_db. Dry-run predeterminado."""
import argparse
import configparser
from datetime import datetime,timezone
import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
ROOT=Path(__file__).resolve().parents[2]
MIGRATIONS=['lab12-perfil.sql','lab12-archivos.sql','lab12-reglas.sql','lab12-seguridad.sql','lab12-reportes.sql','auditoria-preaws-integridad.sql','lab12-auditoria-metricas.sql','lab12-privacidad-reportes.sql']
def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--defaults-file',type=Path);p.add_argument('--expected-host');p.add_argument('--expected-server');p.add_argument('--schema',choices=['admitu_db'],default='admitu_db');p.add_argument('--apply',action='store_true');p.add_argument('--recreate-test-db',action='store_true');p.add_argument('--backup-dir',type=Path,default=ROOT/'work/backups');a=p.parse_args()
    files=[ROOT/'database'/x for x in MIGRATIONS]
    if a.recreate_test_db:files.insert(0,ROOT/'database/TA_Lab11_Script_LosFavoritosDeCueva.sql')
    for file in files:
        if not file.is_file():p.error('Falta '+file.name)
        for m in re.finditer(r'\b(?:USE|CREATE\s+DATABASE(?:\s+IF\s+NOT\s+EXISTS)?|DROP\s+DATABASE(?:\s+IF\s+EXISTS)?)\s+`?([\w]+)',file.read_text(encoding='utf-8-sig'),re.I):
            if m.group(1).lower()!=a.schema:p.error('La migración menciona otro esquema: '+file.name)
        print('PLAN',file.name,'sha256='+hashlib.sha256(file.read_bytes()).hexdigest())
    if not a.apply:print('DRY-RUN: no se conectó a MySQL ni se modificó la base.');return
    if not a.defaults_file or not a.expected_host or not a.expected_server:p.error('--apply exige --defaults-file, --expected-host y --expected-server')
    if not a.defaults_file.is_file():p.error('Falta el archivo privado de cliente MySQL')
    cnf=configparser.ConfigParser(interpolation=None,allow_no_value=True);cnf.read(a.defaults_file,encoding='utf-8')
    if cnf.get('client','host',fallback='localhost').strip('\"\'')!=a.expected_host:p.error('Host configurado distinto al autorizado')
    if not shutil.which('mysql') or not shutil.which('mysqldump'):p.error('mysql y mysqldump deben estar en PATH')
    flags=['--defaults-extra-file='+str(a.defaults_file.resolve()),'--default-character-set=utf8mb4']
    try:
        r=subprocess.run(['mysql',*flags,'-N',a.schema,'-e','SELECT @@hostname,DATABASE()'],capture_output=True,text=True,check=True)
        if r.stdout.strip().split('\t')!=[a.expected_server,a.schema]:p.error('Servidor o esquema no coincide con el entorno de ensayo verificado')
        a.backup_dir.mkdir(parents=True,exist_ok=True);backup=a.backup_dir/('admitu_db-'+datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%S%fZ')+'.sql')
        parcial=backup.with_suffix('.sql.partial')
        with parcial.open('xb') as out:
            os.chmod(parcial,0o600);subprocess.run(['mysqldump',*flags,'--single-transaction','--hex-blob','--routines','--triggers','--no-tablespaces','--set-gtid-purged=OFF',a.schema],stdout=out,stderr=subprocess.PIPE,check=True)
        if not parcial.stat().st_size:sys.exit('ERROR: dump vacío; no se aplicaron migraciones.')
        parcial.rename(backup)
        digest=hashlib.sha256(backup.read_bytes()).hexdigest()
        manifiesto=backup.with_suffix('.sql.sha256')
        with manifiesto.open('x',encoding='utf-8') as out:
            os.chmod(manifiesto,0o600);out.write(digest+'  '+backup.name+'\n')
        print('BACKUP completo y SHA256 creados; conservar ambos fuera del repositorio.')
        for file in files:
            with file.open('rb') as src:subprocess.run(['mysql',*flags,a.schema],stdin=src,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True)
            print('APLICADO',file.name)
        print('OK: migraciones aplicadas solo a admitu_db. El DDL no se revierte con rollback; conserve el backup.')
    except subprocess.CalledProcessError:sys.exit('ERROR MySQL. Operación detenida; revisar detalles por canal privado, sin publicar conexiones ni dump.')
if __name__=='__main__':main()
