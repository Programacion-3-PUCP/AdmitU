#!/usr/bin/env python3
"""Regresión REST con fixtures sintéticos de los 15 RF. Sin --apply no escribe datos."""
import argparse,base64,concurrent.futures,copy,hashlib,json,os,secrets,subprocess,sys,time,urllib.error,urllib.request
from datetime import datetime,timedelta
from pathlib import Path
from zoneinfo import ZoneInfo

PNG=base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=')
def pdf():
    data=b'%PDF-1.4\n';offset=[0]
    for i,obj in enumerate([b'<< /Type /Catalog /Pages 2 0 R >>',b'<< /Type /Pages /Kids [3 0 R] /Count 1 >>',b'<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 200] /Contents 4 0 R >>',b'<< /Length 0 >>\nstream\n\nendstream'],1):
        offset.append(len(data));data+=f'{i} 0 obj\n'.encode()+obj+b'\nendobj\n'
    start=len(data);data+=b'xref\n0 5\n0000000000 65535 f \n'+b''.join(f'{x:010d} 00000 n \n'.encode() for x in offset[1:])
    return data+f'trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n{start}\n%%EOF\n'.encode()

class API:
    def __init__(self,base):self.base=base.rstrip('/')+'/'
    def call(self,path,method='GET',data=None,token=''):
        headers={'Accept':'*/*'}
        if token:headers['Authorization']='Bearer '+token
        if data is not None:headers['Content-Type']='application/json'
        req=urllib.request.Request(self.base+path,data=None if data is None else json.dumps(data).encode(),headers=headers,method=method)
        try:
            with urllib.request.urlopen(req,timeout=45) as r:return r.status,r.headers,r.read()
        except urllib.error.HTTPError as r:return r.code,r.headers,r.read()
    def json(self,path,method='GET',data=None,token=''):
        status,_,body=self.call(path,method,data,token)
        assert 200<=status<300,f'{method} {path}: HTTP {status}'
        return json.loads(body) if body else None
    def login(self,d):return self.json('AutenticacionRS/ingresar','POST',d)['token']

class Suite:
    def __init__(self,args):
        self.a=args;self.api=API(args.base_url);self.cred=json.loads(args.credentials_file.read_text());self.admin=self.api.login(self.cred['admin']);self.ev=self.api.login(self.cred['evaluador'])
        self.tag='T'+secrets.token_hex(4).upper();self.today=datetime.now(ZoneInfo('America/Lima')).date();self.rows=[];self.ids={}
    def check(self,rf,name,fn):
        start=time.perf_counter()
        try:
            result=fn();self.rows.append(dict(rf=rf,case=name,status='PASS',detail=result,seconds=round(time.perf_counter()-start,3)));print('PASS',rf,name,flush=True);return True
        except Exception as e:
            self.rows.append(dict(rf=rf,case=name,status='FAIL',detail=type(e).__name__+': '+str(e)[:200]));print('FAIL',rf,name,type(e).__name__,str(e)[:200],flush=True);return False
    def j(self,path,method='GET',data=None,token=None):return self.api.json(path,method,data,self.admin if token is None else token)
    def insert(self,entity,data):
        i=self.j(f'{entity}RS/insertar{entity}','POST',data);assert isinstance(i,int) and i>0;data['id']=i;return data
    def get(self,entity,i):return self.j(f'{entity}RS/buscar{entity}PorId/{i}')
    def modify(self,entity,data):return self.j(f'{entity}RS/modificar{entity}','PUT',data)
    def negative(self,path,method,data,token):
        status,_,_=self.api.call(path,method,data,token);assert status in (400,401,403,404,409,422),f'HTTP {status}';return {'http':status}
    def catalog(self,entity,data):
        self.insert(entity,data);data['nombre']+=' editado';self.modify(entity,data);assert self.get(entity,data['id'])['nombre']==data['nombre']
        self.j(f'{entity}RS/eliminar{entity}/{data["id"]}','DELETE');assert not self.get(entity,data['id'])['activo']
        self.j(f'CatalogoRS/reactivar/{entity}/{data["id"]}','PUT');assert self.get(entity,data['id'])['activo'];self.ids[entity]=data['id'];return {'id':data['id'],'CRUD_reactivacion':True}
    def setup(self):
        f=self.j('FacultadRS/listarTodasFacultades')[0]['id'];self.sede=self.j('SedeRS/listarTodasSedes')[0]['id']
        assert self.check('RF_04','CRUD modalidad y reactivación persistente',lambda:self.catalog('Modalidad',dict(codigoModalidad=self.tag,nombre='Modalidad '+self.tag,descripcion='Fixture',requiereColegio=False,requiereUniversidad=False,activo=True)))
        assert self.check('RF_06','CRUD carrera y reactivación persistente',lambda:self.catalog('Carrera',dict(facultad={'id':f},codigoCarrera=self.tag,nombre='Carrera '+self.tag,activo=True)))
        def convocatoria():
            d=dict(codigoConvocatoria=self.tag,nombre='Convocatoria '+self.tag,periodo='2026-2',fechaInicio=str(self.today-timedelta(days=1)),fechaFin=str(self.today+timedelta(days=30)),estado='BORRADOR',descripcion='Fixture',activo=True)
            self.insert('Convocatoria',d);d['nombre']+=' editada';self.modify('Convocatoria',d);self.conv=d
            b=dict(d,id=0,codigoConvocatoria=self.tag+'B');self.insert('Convocatoria',b);self.j(f'ConvocatoriaRS/eliminarConvocatoria/{b["id"]}','DELETE');assert not self.get('Convocatoria',b['id'])['activo'];return {'convocatoria':d['id'],'borrador_baja':b['id']}
        assert self.check('RF_01','CRUD convocatoria y baja borrador',convocatoria)
        def requisitos():
            self.cm=self.insert('ConvocatoriaModalidad',dict(convocatoria={'id':self.conv['id']},modalidad={'id':self.ids['Modalidad']},costoInscripcion=100,observacion='Fixture'))
            self.oferta=self.insert('OfertaCarrera',dict(convocatoria={'id':self.conv['id']},carrera={'id':self.ids['Carrera']},cantidadVacantes=10))
            self.req=self.insert('Requisito',dict(codigoRequisito=self.tag,nombre='Certificado '+self.tag,descripcion='Fixture',tipoArchivoRequerido='PDF',tamanioMaximoBytes=5242880,activo=True))
            self.rm=self.insert('RequisitoConvocatoriaModalidad',dict(convocatoriaModalidad={'id':self.cm['id']},requisito={'id':self.req['id']},obligatorio=True,ordenPresentacion=1))
            self.conv['estado']='PUBLICADA';self.modify('Convocatoria',self.conv);return {'asociacion':self.rm['id']}
        assert self.check('RF_07','configurar requisito obligatorio y oferta',requisitos)
        def registro():
            d=dict(nombres='Prueba',apellidoPaterno='Automatica',apellidoMaterno=self.tag,correo=self.tag.lower()+'@example.test',tipoDocumento='DNI',numeroDocumento=str(20000000+secrets.randbelow(60000000)),telefono='999000001',fechaNacimiento='2005-01-01',tieneDiscapacidad=True,numeroCarnetConadis='TEST123')
            self.usercred=dict(correo=d['correo'],clave=secrets.token_urlsafe(24));self.person=self.j('AutenticacionRS/registrar','POST',dict(postulante=d,clave=self.usercred['clave']),token='');self.user=self.api.login(self.usercred)
            self.cred['postulante']=dict(self.usercred,personaId=self.person);self.a.credentials_file.write_text(json.dumps(self.cred));os.chmod(self.a.credentials_file,0o600)
            d=self.get('Postulante',self.person);assert d['tieneDiscapacidad'] and d['numeroCarnetConadis']=='TEST123';d['telefono']='999000002';self.j('PostulanteRS/modificarPostulante','PUT',d,token=self.user);assert self.get('Postulante',self.person)['telefono']=='999000002';return {'persona':self.person,'perfil_durable':True}
        assert self.check('RF_02','registro con contraseña y perfil durable',registro)
    def files(self):
        def personal():
            a=dict(postulanteId=self.person,tipo='DNI',referenciaId=0,nombre='dni.png',mime='image/png',contenidoBase64=base64.b64encode(PNG).decode(),fechaRegistro=str(self.today))
            self.dni=self.j('ArchivoDigitalRS/guardarArchivo','POST',a,token=self.user)
            status,_,raw=self.api.call('ArchivoDigitalRS/descargarArchivo/'+str(self.dni['id']),token=self.user);assert status==200 and raw==PNG
            a['tipo']='FOTOGRAFIA';self.foto=self.j('ArchivoDigitalRS/guardarArchivo','POST',a,token=self.user)
            self.negative('ArchivoDigitalRS/guardarArchivo','POST',dict(a,tipo='DNI',nombre='dni.pdf',mime='application/pdf',contenidoBase64=base64.b64encode(pdf()).decode()),self.user)
            self.negative('ArchivoDigitalRS/guardarArchivo','POST',dict(a,fechaRegistro=str(self.today+timedelta(days=1))),self.user)
            self.j('ArchivoDigitalRS/eliminarArchivo/'+str(self.foto['id']),'DELETE',token=self.user);return {'sha256':hashlib.sha256(raw).hexdigest(),'foto_baja':self.foto['id']}
        self.check('RF_08','bytes DNI/foto descarga y validaciones',personal)
        def certificado():
            a=dict(postulanteId=self.person,tipo='CERTIFICADO',referenciaId=0,nombre='certificado.pdf',mime='application/pdf',contenidoBase64=base64.b64encode(pdf()).decode(),fechaRegistro=str(self.today),institucion='Colegio fixture '+self.tag,anioInicio=2018,anioFin=2022)
            b=self.j('ArchivoDigitalRS/guardarCertificado','POST',a,token=self.user);a['referenciaId']=b['referenciaId'];a['anioFin']=2023;c=self.j('ArchivoDigitalRS/guardarCertificado','POST',a,token=self.user);assert c['id']==b['id']
            assert self.api.call('ArchivoDigitalRS/descargarArchivo/'+str(c['id']),token=self.user)[2]==pdf()
            self.j('ArchivoDigitalRS/eliminarArchivo/'+str(c['id']),'DELETE',token=self.user);return {'archivo':c['id'],'reemplazo_y_baja':True}
        self.check('RF_05','certificado PDF durable modificar y eliminar',certificado)
    def submission(self):
        self.payload=dict(postulacion=dict(postulante={'id':self.person},convocatoria={'id':self.conv['id']},modalidadElegida={'id':self.cm['id']},carreraElegida={'id':self.oferta['id']},estadoActual={'id':2},codigoInscripcion='INS-'+self.tag,sedeId=self.sede),sedeId=self.sede,documentos=[])
        def rollback():
            self.negative('PostulacionRS/enviarPostulacion','POST',self.payload,self.user)
            rows=self.j('PostulacionRS/listarTodasPostulaciones',token=self.user);assert len(rows)==0
            assert self.j('PagoRS/listarTodosPagos',token=self.user)==[];return {'postulaciones':0,'pagos':0}
        self.check('RNF_05','rollback envío sin documento obligatorio',rollback)
        self.payload['documentos']=[dict(requisitoAplicableId=self.rm['id'],nombre='requisito.pdf',mime='application/pdf',contenidoBase64=base64.b64encode(pdf()).decode())]
        def simultaneo():
            with concurrent.futures.ThreadPoolExecutor(2) as e:r=list(e.map(lambda _:self.api.call('PostulacionRS/enviarPostulacion','POST',self.payload,self.user),range(2)))
            status=[x[0] for x in r];assert len([x for x in status if 200<=x<300])==1,status
            assert all(x in (200,201,400,409) for x in status),status
            self.post=self.j('PostulacionRS/listarTodasPostulaciones',token=self.user)[0];assert len(self.j('PostulacionRS/listarTodasPostulaciones',token=self.user))==1
            pagos=self.j('PagoRS/listarTodosPagos',token=self.user);assert len(pagos)==1;self.pago=pagos[0]
            self.doc=self.j('DocumentoPostulacionRS/listarTodosDocumentosPostulacion',token=self.user)[0]
            return {'HTTP':status,'postulacion':self.post['id'],'pagos':len(pagos)}
        assert self.check('O3_O7','dos POST concurrentes una postulación y un pago',simultaneo)
        def otra_carrera():
            career=next(x for x in self.j('CarreraRS/listarTodasCarreras') if x['id']!=self.ids['Carrera'] and x['activo'])
            offer=self.insert('OfertaCarrera',dict(convocatoria={'id':self.conv['id']},carrera={'id':career['id']},cantidadVacantes=10))
            other=copy.deepcopy(self.payload);other['postulacion']['carreraElegida']={'id':offer['id']};other['postulacion']['codigoInscripcion']='DIST-'+self.tag
            result=self.negative('PostulacionRS/enviarPostulacion','POST',other,self.user)
            assert len(self.j('PostulacionRS/listarTodasPostulaciones',token=self.user))==1
            assert len(self.j('PagoRS/listarTodosPagos',token=self.user))==1
            return dict(result,carrera_y_codigo_distintos=True,postulaciones=1,pagos=1)
        self.check('O7','otra carrera y otro código en misma convocatoria rechazados',otra_carrera)
    def payments_observation(self):
        def voucher():
            a=dict(postulanteId=self.person,tipo='VOUCHER',referenciaId=self.pago['id'],nombre='voucher.png',mime='image/png',contenidoBase64=base64.b64encode(PNG).decode(),fechaRegistro=str(self.today),codigoOperacion='OP-'+self.tag,monto=100)
            r=self.j('ArchivoDigitalRS/guardarVoucher','POST',a,token=self.user);assert self.api.call('ArchivoDigitalRS/descargarArchivo/'+str(r['id']),token=self.user)[2]==PNG
            again=self.j('ArchivoDigitalRS/guardarVoucher','POST',a,token=self.user);assert again['id']==r['id'];assert len(self.j('PagoRS/listarTodosPagos',token=self.user))==1;return {'pago':self.pago['id'],'doble_envio_un_solo_voucher':True}
        assert self.check('RF_11','voucher bytes durable sin duplicar',voucher)
        def pago():
            d=self.get('Pago',self.pago['id']);d['estadoPago']='APROBADO';self.j('PagoRS/modificarPago','PUT',d,token=self.ev);p=self.get('Pago',d['id']);assert p['estadoPago']=='APROBADO' and p['fechaPago']
            self.negative('PagoRS/modificarPago','PUT',dict(p,estadoPago='RECHAZADO'),self.ev);return {'fecha':p['fechaPago'],'inmutable':True}
        assert self.check('RF_12','aprobar voucher con fecha y bloquear cambios',pago)
        def observacion():
            self.doc['estadoDocumento']='OBSERVADO';self.modify('DocumentoPostulacion',self.doc)
            d=self.insert('DocumentoObservacion',dict(documento={'id':self.doc['id']},evaluador={'id':self.cred['evaluador']['personaId']},tipoObservacion='ILEGIBLE',descripcion='Texto de prueba',fechaObservacion=str(self.today),estadoObservacion='PENDIENTE'))
            d['descripcion']='Texto corregido';self.modify('DocumentoObservacion',d);assert self.get('DocumentoObservacion',d['id'])['descripcion']=='Texto corregido';self.observacion=d;return {'observacion':d['id']}
        self.check('RF_10','registrar consultar modificar observación',observacion)
    def reports(self):
        for rf,kind in [('RF_03','Ingresos'),('RF_09','Observados'),('RF_15','Vacantes')]:
            def report(kind=kind):
                status,headers,data=self.api.call(f'ReporteRS/generar{kind}?convocatoriaId={self.conv["id"]}',token=self.admin)
                assert status==200 and headers.get('Content-Type','').startswith('application/pdf') and data.startswith(b'%PDF-')
                file=self.a.output.parent/f'{kind}-{self.tag}.pdf';file.write_bytes(data)
                if kind=='Ingresos':
                    text=subprocess.check_output(['pdftotext',str(file),'-'],text=True);assert '100.00' in text or '100,00' in text
                return {'bytes':len(data),'convocatoria':self.conv['id']}
            self.check(rf,'Jasper PDF filtrado '+kind,report)
    def history_carnet(self):
        def history():
            before=self.j('PostulacionHistorialRS/listarTodosPostulacionHistorial',token=self.user)
            d=self.get('Postulacion',self.post['id']);d['estadoActual']={'id':4};self.modify('Postulacion',d)
            after=self.j('PostulacionHistorialRS/listarTodosPostulacionHistorial',token=self.user);assert len(after)>len(before);return {'historial':len(after)}
        self.check('RF_13','transición con historial durable',history)
        def carnet():
            d=dict(postulacion={'id':self.post['id']},sede={'id':self.sede},codigoCarne='CAR-'+self.tag,fechaGeneracion=str(self.today),fechaInicioVigencia=str(self.today),fechaFinVigencia=str(self.today+timedelta(days=30)),aulaExamen='A101')
            self.negative('CarnePostulanteRS/insertarCarnePostulante','POST',d,self.admin)
            self.doc['estadoDocumento']='APROBADO';self.modify('DocumentoPostulacion',self.doc)
            x=self.get('Postulacion',self.post['id']);x['estadoActual']={'id':5};self.modify('Postulacion',x)
            self.insert('CarnePostulante',d);assert self.get('CarnePostulante',d['id'])['id']==d['id'];self.j(f'CarnePostulanteRS/eliminarCarnePostulante/{d["id"]}','DELETE');return {'carne':d['id'],'inapto_rechazado':True,'emitir_baja':True}
        self.check('RF_14','elegibilidad emisión búsqueda y baja carné',carnet)
    def security(self):
        self.check('RNF_03','REST privado sin token',lambda:self.negative('PagoRS/listarTodosPagos','GET',None,''))
        self.check('RNF_03','perfil ajeno denegado',lambda:self.negative('PostulanteRS/buscarPostulantePorId/1','GET',None,self.user))
        self.check('RNF_03','postulante no autoaprueba pago',lambda:self.negative('PagoRS/modificarPago','PUT',{},self.user))
        self.check('RNF_03','postulante no modifica catálogo',lambda:self.negative('CarreraRS/modificarCarrera','PUT',{},self.user))
    def allowed_boundaries(self):
        def another_applicant():
            cred=dict(correo=self.tag.lower()+'-otro@example.test',clave=secrets.token_urlsafe(24))
            persona=dict(nombres='Otra prueba',apellidoPaterno='Automatica',correo=cred['correo'],tipoDocumento='DNI',numeroDocumento=str(20000000+secrets.randbelow(60000000)),fechaNacimiento='2005-01-01')
            person=self.j('AutenticacionRS/registrar','POST',dict(postulante=persona,clave=cred['clave']),token='')
            token=self.api.login(cred);payload=copy.deepcopy(self.payload);payload['postulacion']['postulante']={'id':person};payload['postulacion']['codigoInscripcion']='OTRO-'+self.tag
            id=self.j('PostulacionRS/enviarPostulacion','POST',payload,token=token)
            assert len(self.j('PostulacionRS/listarTodasPostulaciones',token=token))==1
            assert len(self.j('PagoRS/listarTodosPagos',token=token))==1
            return {'postulacion':id,'otro_postulante_misma_convocatoria':True}
        self.check('O7','otro postulante sí puede usar la misma convocatoria',another_applicant)
        def another_call():
            call=dict(self.conv,id=0,codigoConvocatoria=self.tag+'D',nombre='Otra convocatoria '+self.tag)
            self.insert('Convocatoria',call)
            cm=self.insert('ConvocatoriaModalidad',dict(convocatoria={'id':call['id']},modalidad={'id':self.ids['Modalidad']},costoInscripcion=100))
            offer=self.insert('OfertaCarrera',dict(convocatoria={'id':call['id']},carrera={'id':self.ids['Carrera']},cantidadVacantes=10))
            payload=copy.deepcopy(self.payload);payload['postulacion'].update(convocatoria={'id':call['id']},modalidadElegida={'id':cm['id']},carreraElegida={'id':offer['id']},codigoInscripcion='CONV-'+self.tag);payload['documentos']=[]
            id=self.j('PostulacionRS/enviarPostulacion','POST',payload,token=self.user)
            assert len(self.j('PostulacionRS/listarTodasPostulaciones',token=self.user))==2
            return {'postulacion':id,'mismo_postulante_otra_convocatoria':True}
        self.check('O7','mismo postulante sí puede usar otra convocatoria',another_call)

    def run(self):
        try:self.setup();self.files();self.submission();self.security();self.payments_observation();self.reports();self.history_carnet();self.allowed_boundaries()
        finally:
            self.a.output.parent.mkdir(parents=True,exist_ok=True);self.a.output.write_text(json.dumps(self.rows,indent=2,ensure_ascii=False)+'\n')
        return 1 if any(r['status']=='FAIL' for r in self.rows) else 0

def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--apply',action='store_true');p.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/');p.add_argument('--credentials-file',type=Path,default=Path('work/test-credentials.json'));p.add_argument('--output',type=Path,default=Path('work/evidencias/regresion.json'));a=p.parse_args()
    if not a.apply:print('DRY-RUN: se crearían fixtures sintéticos propios; ninguna escritura ejecutada.');return 0
    a.output.parent.mkdir(parents=True,exist_ok=True)
    return Suite(a).run()
if __name__=='__main__':sys.exit(main())
