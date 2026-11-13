#!/usr/bin/env python3
"""Auditoría REST con cinco identidades sintéticas y huellas SQL antes/después.
Solo ejecuta mutaciones con --apply, MySQL loopback y esquema admitu_db.
Las credenciales, tokens y filas SQL nunca se escriben en evidencias.
"""
import argparse, base64, copy, hashlib, json, secrets, subprocess, sys
from pathlib import Path
from regresion import API, Suite, PNG

TABLES = ('antecedente_academico','apoderado','archivo_digital','carne_postulante',
          'carrera','convocatoria','convocatoria_etapa','convocatoria_modalidad',
          'convocatoria_postulante','documento_observacion','documento_postulacion',
          'estado_postulacion','etapa','evaluador','facultad','institucion_educativa',
          'modalidad','notificacion','oferta_carrera','pago','pais','persona',
          'postulacion','postulacion_historial','postulante','requisito',
          'requisito_convocatoria_modalidad','sede')
# Cuenta/sesión quedan fuera: los negativos de login registran fallos, bloqueo y
# revocación por diseño. Todas las tablas de negocio, incluidos catálogos, sí se
# contrastan para detectar cualquier escritura previa a una respuesta denegada.

class Auditoria:
    def __init__(self, args):
        self.a=args;self.results=[];self.api=API(args.base_url)
        self.credentials=json.loads(args.credentials_file.read_text())
        self.tokens={role:self.api.login(self.credentials[role]) for role in ('admin','evaluador','evaluador2')}
        self.ev1=int(self.credentials['evaluador']['personaId']);self.ev2=int(self.credentials['evaluador2']['personaId'])
        assert self.ev1!=self.ev2,'Se requieren dos evaluadores diferentes'
        assert self.sql('SELECT DATABASE();').strip()=='admitu_db'
        # Cada fila se convierte en huella dentro del servidor antes de salir de MySQL.
        self.hash_queries=[]
        for table in TABLES:
            columns=self.sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='admitu_db' AND TABLE_NAME='"+table+"' ORDER BY ORDINAL_POSITION;").splitlines()
            assert columns,table
            expressions=["IFNULL(HEX(`"+c+"`),'NULL')" for c in columns]
            self.hash_queries.append("SELECT SHA2(CONCAT_WS('|',"+','.join(expressions)+"),256) FROM `"+table+"` ORDER BY 1;")
    def sql(self,query):
        proc=subprocess.run(['mysql','--defaults-extra-file='+str(self.a.mysql_defaults_file.resolve()),'--protocol=TCP','--host=127.0.0.1','--batch','--raw','--skip-column-names','admitu_db'],input=query,text=True,capture_output=True)
        if proc.returncode:raise RuntimeError('Falló SQL de ensayo; consulte diagnóstico privado')
        return proc.stdout
    def snapshot(self):
        return hashlib.sha256(self.sql('\n'.join(self.hash_queries)).encode()).hexdigest()
    def check(self,name,fn):
        try:
            detail=fn();self.results.append(dict(case=name,status='PASS',detail=detail));print('PASS',name,flush=True)
        except Exception as exc:
            self.results.append(dict(case=name,status='FAIL',detail=type(exc).__name__+': '+str(exc)[:160]));print('FAIL',name,type(exc).__name__,flush=True)
    def expect(self,name,path,status=403,method='GET',data=None,token=''):
        def run():
            before=self.snapshot();actual,_,_=self.api.call(path,method,data,token);after=self.snapshot()
            assert actual==status,f'HTTP esperado {status}; obtenido {actual}'
            assert before==after,'La base de negocio cambió tras un rechazo'
            return dict(http=actual,sql_before=before,sql_after=after,unchanged=True)
        self.check(name,run)
    def json(self,path,method='GET',data=None,token=None):
        return self.api.json(path,method,data,self.tokens['admin'] if token is None else token)
    def prepare(self):
        # Reutiliza fixtures de RF; todos los casos aquí añaden su propio contraste SQL.
        suite=Suite(self.a);suite.setup();suite.submission()
        self.fixture=suite;self.results.extend(suite.rows)
        self.user=suite.user;self.person=suite.person;self.post=suite.post;self.doc=suite.doc;self.pago=suite.pago
        self.payload=suite.payload
        self.json(f'PostulacionRS/asignarEvaluador/{self.post["id"]}/{self.ev1}','PUT')
        credential=dict(correo='audit-'+secrets.token_hex(6)+'@example.test',clave=secrets.token_urlsafe(24))
        person=dict(nombres='Auditoria',apellidoPaterno='Segundo',apellidoMaterno='Sintetico',correo=credential['correo'],tipoDocumento='DNI',numeroDocumento=str(20000000+secrets.randbelow(60000000)),fechaNacimiento='2000-01-01')
        self.person2=self.json('AutenticacionRS/registrar','POST',dict(postulante=person,clave=credential['clave']),token='')
        self.user2cred=credential;self.user2=self.api.login(credential)
        payload=copy.deepcopy(self.payload);payload['postulacion']['postulante']={'id':self.person2}
        payload['postulacion']['codigoInscripcion']='AUD2-'+secrets.token_hex(8).upper()
        self.post2id=self.json('PostulacionRS/enviarPostulacion','POST',payload,token=self.user2)
        self.json(f'PostulacionRS/asignarEvaluador/{self.post2id}/{self.ev2}','PUT')
        self.document_file=int(self.sql("SELECT id FROM archivo_digital WHERE tipo='DOCUMENTO' AND referencia_id="+str(self.doc['id'])+";").strip())
        self.file=self.json('ArchivoDigitalRS/guardarArchivo','POST',dict(postulanteId=self.person,tipo='DNI',referenciaId=0,nombre='auditoria.png',mime='image/png',contenidoBase64=base64.b64encode(PNG).decode(),fechaRegistro=str(suite.today)),token=self.user)
    def matrix(self):
        ev1=self.tokens['evaluador'];ev2=self.tokens['evaluador2'];admin=self.tokens['admin']
        mutation=dict(self.post,estadoActual={'id':3})
        for role,token in [('anonimo',''),('postulante1',self.user),('postulante2',self.user2),('evaluador1',ev1),('evaluador2',ev2),('admin',admin)]:
            status=401 if not token else 403
            for method,path,data in [('POST','PostulacionHistorialRS/insertarPostulacionHistorial',{}),('PUT','PostulacionHistorialRS/modificarPostulacionHistorial',{}),('DELETE','PostulacionHistorialRS/eliminarPostulacionHistorial/1',None)]:
                self.expect(f'H16 historial inmutable {role} {method}',path,status,method,data,token)
        for role,token in [('evaluador1',ev1),('evaluador2',ev2)]:
            self.expect(f'H01 {role} no crea postulación','PostulacionRS/insertarPostulacion',403,'POST',self.post,token)
            self.expect(f'H01 {role} no envía por tercero','PostulacionRS/enviarPostulacion',403,'POST',self.payload,token)
            self.expect(f'H01 {role} no crea pago','PagoRS/insertarPago',403,'POST',self.pago,token)
            self.expect(f'H01 {role} no asigna evaluadores',f'PostulacionRS/asignarEvaluador/{self.post["id"]}/{self.ev2}',403,'PUT',None,token)
            self.expect(f'{role} no modifica catálogo','CarreraRS/modificarCarrera',403,'PUT',{},token)
            self.expect(f'{role} no emite carné','CarnePostulanteRS/insertarCarnePostulante',403,'POST',{},token)
            self.expect(f'{role} no abre ingresos','ReporteRS/generarIngresos',403,token=token)
            self.expect(f'{role} no abre vacantes','ReporteRS/generarVacantes',403,token=token)
        self.expect('H01 evaluador ajeno no modifica estado','PostulacionRS/modificarPostulacion',403,'PUT',mutation,ev2)
        self.expect('H01 evaluador ajeno no valida pago','PagoRS/modificarPago',403,'PUT',dict(self.pago,estadoPago='APROBADO'),ev2)
        self.expect('H01 evaluador ajeno no valida documento','DocumentoPostulacionRS/modificarDocumentoPostulacion',403,'PUT',dict(self.doc,estadoDocumento='APROBADO'),ev2)
        for label,token in [('anonimo',''),('postulante2',self.user2),('evaluador2',ev2)]:
            self.expect('H07 archivo privado '+label,'ArchivoDigitalRS/descargarArchivo/'+str(self.file['id']),401 if not token else 403,token=token)
        self.expect('H07 bytes documento expediente ajeno','ArchivoDigitalRS/descargarArchivo/'+str(self.document_file),403,token=ev2)
        for label,path in [('postulación',f'PostulacionRS/buscarPostulacionPorId/{self.post["id"]}'),('pago',f'PagoRS/buscarPagoPorId/{self.pago["id"]}'),('documento',f'DocumentoPostulacionRS/buscarDocumentoPostulacionPorId/{self.doc["id"]}')]:
            self.expect('Propietario cruzado '+label,path,403,token=self.user2)
        cross=dict(postulanteId=self.person2,tipo='DOCUMENTO',referenciaId=self.doc['id'],nombre='ajeno.pdf',mime='application/pdf',contenidoBase64=base64.b64encode(PNG).decode())
        self.expect('Archivo owner propio no permite referencia documento ajeno','ArchivoDigitalRS/guardarArchivo',403,'POST',cross,self.user2)
        cross.update(tipo='VOUCHER',referenciaId=self.pago['id'],nombre='voucher.png',mime='image/png',codigoOperacion='CROSS1234',monto=100)
        self.expect('Voucher owner propio no permite referencia pago ajeno','ArchivoDigitalRS/guardarVoucher',403,'POST',cross,self.user2)
        self.expect('H07 archivos propietario no asignado','ArchivoDigitalRS/listarArchivosPorPostulante/'+str(self.person),403,token=ev2)
        self.expect('Perfil ajeno no consultable',f'PostulanteRS/buscarPostulantePorId/{self.person}',403,token=self.user2)
        self.expect('Postulante no modifica estados','PostulacionRS/modificarPostulacion',403,'PUT',mutation,self.user)
        self.expect('Postulante no aprueba pago','PagoRS/modificarPago',403,'PUT',dict(self.pago,estadoPago='APROBADO'),self.user)
        other=copy.deepcopy(self.payload);other['postulacion']['postulante']={'id':self.person2}
        self.expect('Postulante no envía identidad ajena','PostulacionRS/enviarPostulacion',403,'POST',other,self.user)
        obs=dict(documento={'id':self.doc['id']},evaluador={'id':self.ev2},tipoObservacion='ILEGIBLE',descripcion='Observación sintética',fechaObservacion=str(self.fixture.today),estadoObservacion='PENDIENTE')
        self.expect('H17 suplantación autor observación','DocumentoObservacionRS/insertarDocumentoObservacion',403,'POST',obs,ev1)
        self.expect('H17 no observa documento fuera ámbito','DocumentoObservacionRS/insertarDocumentoObservacion',403,'POST',obs,ev2)
        self.expect('H17 admin no firma como evaluador','DocumentoObservacionRS/insertarDocumentoObservacion',403,'POST',obs,admin)
        obs['evaluador']={'id':self.ev1}
        def observation():
            incoming=dict(obs,fechaObservacion='2099-01-01',estadoObservacion='SUBSANADA',fechaSubsanacion='2099-01-02',comentarioSubsanacion='Metadatos arbitrarios del cliente')
            oid=self.json('DocumentoObservacionRS/insertarDocumentoObservacion','POST',incoming,ev1)
            row=self.json(f'DocumentoObservacionRS/buscarDocumentoObservacionPorId/{oid}')
            assert row['evaluador']['id']==self.ev1
            assert row['fechaObservacion']==str(self.fixture.today) and row['estadoObservacion']=='PENDIENTE'
            assert row.get('fechaSubsanacion') is None and row.get('comentarioSubsanacion') is None
            self.observation=row
            return dict(observacion=oid,author_bound_to_token=True,initial_state_date_from_server=True)
        self.check('H17 observación legítima autor de token',observation)
        if hasattr(self,'observation'):
            spoof=dict(self.observation,evaluador={'id':self.ev2})
            self.expect('H17 autor original no puede alterarse','DocumentoObservacionRS/modificarDocumentoObservacion',403,'PUT',spoof,ev1)
            self.json(f'PostulacionRS/asignarEvaluador/{self.post["id"]}/{self.ev2}','PUT')
            rewrite=dict(self.observation,descripcion='Intento de reescribir firma ajena')
            self.expect('H17 nuevo asignado no reescribe texto ajeno','DocumentoObservacionRS/modificarDocumentoObservacion',403,'PUT',rewrite,ev2)
            def resolve():
                body=dict(self.observation,estadoObservacion='SUBSANADA',fechaSubsanacion='2099-01-01',comentarioSubsanacion='Verificación sintética autorizada')
                self.json('DocumentoObservacionRS/modificarDocumentoObservacion','PUT',body,ev2)
                row=self.json(f'DocumentoObservacionRS/buscarDocumentoObservacionPorId/{self.observation["id"]}')
                assert row['evaluador']['id']==self.ev1 and row['estadoObservacion']=='SUBSANADA' and row['fechaSubsanacion']==str(self.fixture.today)
                assert row['fechaObservacion']==self.observation['fechaObservacion']
                self.resolved=row
                return dict(author_preserved=True,date_from_server=True,reviewer_assigned=True)
            self.check('RF10 subsanación por reasignado conserva autor y fecha servidor',resolve)
            if hasattr(self,'resolved'):
                reopen=dict(self.resolved,estadoObservacion='PENDIENTE')
                self.expect('RF10 no reabrir subsanación histórica','DocumentoObservacionRS/modificarDocumentoObservacion',403,'PUT',reopen,ev2)
            self.json(f'PostulacionRS/asignarEvaluador/{self.post["id"]}/{self.ev1}','PUT')
        def own_file():
            status,_,raw=self.api.call('ArchivoDigitalRS/descargarArchivo/'+str(self.file['id']),token=ev1)
            assert status==200 and raw==PNG
            return dict(http=status,bytes=len(raw))
        self.check('H07 evaluador asignado descarga bytes reales',own_file)
        def assignment():
            row=self.json('PostulacionRS/buscarPostulacionPorId/'+str(self.post['id']),token=ev1)
            assert row['evaluadorId']==self.ev1
            self.json(f'PostulacionRS/asignarEvaluador/{self.post["id"]}/{self.ev2}','PUT')
            return dict(assignment_reloaded=True,reassigned=True)
        self.check('H08 asignación persistida y reasignación',assignment)
        self.expect('H01 revocación asignación impide escribir','PostulacionRS/modificarPostulacion',403,'PUT',mutation,ev1)
        self.expect('H07 revocación asignación impide descargar','ArchivoDigitalRS/descargarArchivo/'+str(self.file['id']),403,token=ev1)
        self.json(f'PostulacionRS/asignarEvaluador/{self.post["id"]}/{self.ev1}','PUT')
    def sessions(self):
        # Cada alteración SQL de sesión se limita a las identidades sintéticas de esta ejecución.
        expired=self.api.login(self.user2cred);digest=hashlib.sha256(expired.encode()).hexdigest()
        self.sql("UPDATE sesion_usuario SET vence=UTC_TIMESTAMP()-INTERVAL 1 SECOND WHERE token_hash='"+digest+"';")
        self.expect('Sesión expirada retorna 401','PagoRS/listarTodosPagos',401,token=expired)
        for _ in range(5):self.api.call('AutenticacionRS/ingresar','POST',dict(self.user2cred,clave='Incorrecta para auditoría'))
        self.expect('Cuenta bloqueada rechaza contraseña válida','AutenticacionRS/ingresar',401,'POST',self.user2cred)
        self.expect('Cuenta bloqueada revoca acceso token previo','PagoRS/listarTodosPagos',401,token=self.user2)
        self.check('Logout revoca sesión',lambda:self.logout())
        self.expect('Logout token revocado retorna 401','PagoRS/listarTodosPagos',401,token=self.user)
        self.expect('Token malformado retorna 401','PagoRS/listarTodosPagos',401,token='invalido')
    def logout(self):
        status,_,_=self.api.call('AutenticacionRS/salir','POST',token=self.user)
        assert status==204
        return dict(http=status)
    def run(self):
        try:self.prepare();self.matrix();self.sessions()
        finally:
            self.a.output.parent.mkdir(parents=True,exist_ok=True)
            negative=sum(isinstance(row.get('detail'),dict) and row['detail'].get('unchanged') is True for row in self.results)
            passed=sum(row['status']=='PASS' for row in self.results)
            summary=dict(cases=len(self.results),passed=passed,failed=len(self.results)-passed,negative_http_sql=negative,business_tables=len(TABLES))
            self.a.output.write_text(json.dumps(dict(environment='Codex Cloud propio; no WSL/AWS',identities=dict(admin=1,evaluadores=2,postulantes=2),snapshot_tables=list(TABLES),snapshot_exclusions=['cuenta_usuario','sesion_usuario'],cases=self.results,summary=summary),ensure_ascii=False,indent=2)+'\n')
            print(f'{passed}/{len(self.results)} PASS; {negative} negativos HTTP/SQL; {len(TABLES)} tablas de negocio contrastadas.',flush=True)
        return 1 if any(r['status']=='FAIL' for r in self.results) else 0

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply',action='store_true');parser.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/')
    parser.add_argument('--credentials-file',type=Path,required=True);parser.add_argument('--mysql-defaults-file',type=Path,required=True)
    parser.add_argument('--output',type=Path,default=Path('work/evidencias/auditoria-seguridad.json'))
    args=parser.parse_args()
    if not args.apply:print('DRY-RUN: use --apply solo en admitu_db de ensayo local con respaldo.');return 0
    return Auditoria(args).run()
if __name__=='__main__':sys.exit(main())
