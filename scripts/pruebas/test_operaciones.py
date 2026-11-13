"""Pruebas de los bloqueos del restaurador; usan clientes MySQL simulados, sin servidor."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT/'scripts/despliegue/restaurar.py'

class RestauracionSegura(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.base = Path(self.tmp.name)
        self.dump = self.base/'respaldo.sql'
        self.dump.write_text('USE admitu_db;\nCREATE TABLE prueba (id INT);\n', encoding='utf-8')
        self.cnf = self.base/'cliente.cnf'
        self.cnf.write_text('[client]\nhost=127.0.0.1\nuser=fixture\n', encoding='utf-8')
        self.log = self.base/'invocaciones.jsonl'
        self.env = {**os.environ, 'PATH': str(self.base)+os.pathsep+os.environ['PATH'], 'TEST_OPERACIONES_LOG': str(self.log)}
        for name in ('mysql', 'mysqldump'):
            exe = self.base/name
            exe.write_text('#!'+sys.executable+'\n'+'''
import json, os, sys
from pathlib import Path
nombre=Path(sys.argv[0]).name
with open(os.environ['TEST_OPERACIONES_LOG'],'a') as f:f.write(json.dumps([nombre,*sys.argv[1:]])+'\\n')
if nombre=='mysqldump':
    if os.environ.get('TEST_FALLO_DUMP'):sys.exit(2)
    print('USE admitu_db; -- estado previo')
elif '-e' in sys.argv:print(os.environ.get('TEST_SERVIDOR','ensayo')+'\\tadmitu_db')
else:sys.stdin.read()
''', encoding='utf-8')
            exe.chmod(0o700)

    def run_script(self, *extra):
        return subprocess.run([sys.executable, str(SCRIPT), '--backup-file', str(self.dump), '--sha256', hashlib.sha256(self.dump.read_bytes()).hexdigest(), *extra], capture_output=True, text=True, env=self.env)

    def apply_args(self):
        return ['--apply', '--defaults-file', str(self.cnf), '--expected-host', '127.0.0.1', '--expected-server', 'ensayo', '--backup-dir', str(self.base/'backups')]

    def calls(self):
        return [json.loads(x) for x in self.log.read_text().splitlines()] if self.log.exists() else []

    def test_dry_run_no_abre_mysql(self):
        r = self.run_script()
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertEqual(self.calls(), [])

    def test_hash_incorrecto_no_abre_mysql(self):
        r = self.run_script('--sha256', '0'*64, *self.apply_args())
        self.assertNotEqual(r.returncode, 0)
        self.assertEqual(self.calls(), [])

    def test_otro_esquema_no_abre_mysql(self):
        self.dump.write_text('USE otra_base;\n')
        r = self.run_script(*self.apply_args())
        self.assertNotEqual(r.returncode, 0)
        self.assertEqual(self.calls(), [])

    def test_backup_con_blob_binario_no_requiere_utf8(self):
        self.dump.write_bytes(b"USE admitu_db;\nINSERT INTO archivo VALUES ('\x89PNG\x00\xff');\n")
        r = self.run_script()
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertEqual(self.calls(), [])

    def test_host_configurado_distinto_no_abre_mysql(self):
        r = self.run_script(*self.apply_args(), '--expected-host', 'otro')
        self.assertNotEqual(r.returncode, 0)
        self.assertEqual(self.calls(), [])

    def test_servidor_distinto_no_respalda_ni_restaura(self):
        self.env['TEST_SERVIDOR'] = 'ajeno'
        r = self.run_script(*self.apply_args())
        self.assertNotEqual(r.returncode, 0)
        self.assertEqual([x[0] for x in self.calls()], ['mysql'])

    def test_backup_fallido_no_restaura(self):
        self.env['TEST_FALLO_DUMP'] = '1'
        r = self.run_script(*self.apply_args())
        self.assertNotEqual(r.returncode, 0)
        self.assertEqual([x[0] for x in self.calls()], ['mysql', 'mysqldump'])
        self.assertFalse(list((self.base/'backups').glob('*.sql')))

    def test_respaldo_completo_antes_de_restaurar(self):
        r = self.run_script(*self.apply_args())
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertEqual([x[0] for x in self.calls()], ['mysql', 'mysqldump', 'mysql'])
        self.assertEqual(len(list((self.base/'backups').glob('*.sql'))), 1)
        self.assertEqual(len(list((self.base/'backups').glob('*.sql.sha256'))), 1)
        self.assertFalse(list((self.base/'backups').glob('*.partial')))

if __name__ == '__main__':
    unittest.main()
