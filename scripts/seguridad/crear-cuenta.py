#!/usr/bin/env python3
"""Provisiona una cuenta sin contraseña en argumentos/logs. Ejecutar en infraestructura propia verificada."""
import argparse,base64,getpass,hashlib,os,secrets,subprocess,sys
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--defaults-file',type=Path,required=True)
p.add_argument('--correo',required=True)
p.add_argument('--rol',choices=['admin','evaluador','postulante'],required=True)
p.add_argument('--persona-id',type=int,default=0)
p.add_argument('--mysql',default='mysql')
a=p.parse_args()
if not a.defaults_file.is_file():p.error('Falta el archivo privado MySQL')
if a.rol!='admin' and a.persona_id<=0:p.error('--persona-id debe identificar persona existente del rol')
if len(a.correo)>150 or '@' not in a.correo or any(c in a.correo for c in "'\\\r\n\0"):p.error('Correo no válido')
clave=os.environ.get('ADMITU_NUEVA_CLAVE') or getpass.getpass('Nueva contraseña (12–128 caracteres): ')
if not 12<=len(clave)<=128:p.error('Contraseña debe tener entre 12 y 128 caracteres')
sal=secrets.token_bytes(16)
derivado=hashlib.pbkdf2_hmac('sha256',clave.encode(),sal,600000)
clave_hash='pbkdf2_sha256$600000$'+base64.b64encode(sal).decode()+'$'+base64.b64encode(derivado).decode()
persona=str(a.persona_id) if a.persona_id>0 else 'NULL'
sql="START TRANSACTION;CALL INSERTAR_CUENTA_USUARIO("+persona+",'"+a.correo.strip().lower()+"','"+a.rol+"','"+clave_hash+"');COMMIT;\n"
r=subprocess.run([a.mysql,'--defaults-extra-file='+str(a.defaults_file.resolve()),'--default-character-set=utf8mb4','--database=admitu_db'],input=sql,text=True,capture_output=True)
if r.returncode:sys.exit('No se pudo provisionar la cuenta. Comprueba destino, migración, persona/rol y correo único por canal privado.')
print('Cuenta provisionada. Entrega credenciales por un canal privado.')
