#!/usr/bin/env python3
"""Pruebas reales de autenticación/roles/propietario. No imprime claves, tokens ni cuerpos personales."""
import argparse,json,secrets,urllib.request,urllib.error
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/')
p.add_argument('--credentials-file',type=Path,required=True,help='JSON PRIVADO con admin/evaluador, correo y clave')
p.add_argument('--output',type=Path,default=Path('work/seguridad-api.json'))
p.add_argument('--apply',action='store_true',help='crea dos cuentas ficticias de postulante para negativos/ownership')
a=p.parse_args()
if not a.apply:
 print('DRY-RUN: añada --apply para crear fixtures en el entorno de ensayo propio.');raise SystemExit(0)
creds=json.loads(a.credentials_file.read_text());results=[]
def call(path,method='GET',data=None,token=''):
 headers={'Accept':'*/*'}
 if token:headers['Authorization']='Bearer '+token
 body=None
 if data is not None:headers['Content-Type']='application/json';body=json.dumps(data).encode()
 req=urllib.request.Request(a.base_url.rstrip('/')+'/'+path,data=body,method=method,headers=headers)
 try:
  with urllib.request.urlopen(req,timeout=30)as r:return r.status,r.read()
 except urllib.error.HTTPError as r:return r.code,r.read()
def check(name,fn):
 try:
  detail=fn();results.append({'case':name,'status':'PASS','detail':detail});print('PASS',name)
 except Exception as e:results.append({'case':name,'status':'FAIL','detail':type(e).__name__});print('FAIL',name,type(e).__name__)
def expect(path,status,method='GET',data=None,token=''):
 actual,_=call(path,method,data,token);assert actual==status,f'HTTP {actual}';return {'http':actual}
def login(c,expected=None):
 s,b=call('AutenticacionRS/ingresar','POST',c);assert s==200,f'login HTTP{s}';body=json.loads(b);assert expected is None or body['rol']==expected;return body['token']
check('Pago privado sin sesión',lambda:expect('PagoRS/listarTodosPagos',401))
check('Reporte privado sin sesión',lambda:expect('ReporteRS/generarIngresos',401))
check('Contraseña incorrecta',lambda:expect('AutenticacionRS/ingresar',401,'POST',{'correo':creds['admin']['correo'],'clave':'Contraseña deliberadamente incorrecta'}))
admin=login(creds['admin']);evaluator=login(creds['evaluador'])
check('Admin consulta pagos',lambda:expect('PagoRS/listarTodosPagos',200,token=admin))
check('Evaluador no cambia catálogos',lambda:expect('CarreraRS/modificarCarrera',403,'PUT',{},evaluator))
users=[]
for i in range(2):
 tag=secrets.token_hex(6);c={'correo':f'prueba-{tag}@example.test','clave':secrets.token_urlsafe(24)}
 postulante={'nombres':'Prueba Seguridad','apellidoPaterno':'Laboratorio','apellidoMaterno':'Temporal','correo':c['correo'],'tipoDocumento':'DNI','numeroDocumento':str(secrets.randbelow(90000000)+10000000),'telefono':'999999999','fechaNacimiento':'2000-01-01'}
 s,b=call('AutenticacionRS/registrar','POST',{'postulante':postulante,'clave':c['clave']});assert s==200,f'registro HTTP{s}'
 users.append({'id':json.loads(b),'token':login(c,'postulante'),'cred':c,'postulante':postulante})
u,v=users
check('Postulante no aprueba su pago',lambda:expect('PagoRS/modificarPago',403,'PUT',{},u['token']))
check('Postulante no valida documento',lambda:expect('DocumentoPostulacionRS/modificarDocumentoPostulacion',403,'PUT',{},u['token']))
check('Postulante no altera estados',lambda:expect('PostulacionRS/modificarPostulacion',403,'PUT',{},u['token']))
check('Postulante no consulta perfil ajeno',lambda:expect('PostulanteRS/buscarPostulantePorId/'+str(v['id']),403,token=u['token']))
check('Postulante no cambia perfil ajeno',lambda:expect('PostulanteRS/modificarPostulante',403,'PUT',dict(v['postulante'],id=v['id']),u['token']))
check('Postulante no consulta archivos ajenos',lambda:expect('ArchivoDigitalRS/listarArchivosPorPostulante/'+str(v['id']),403,token=u['token']))
check('Postulante no abre ingresos',lambda:expect('ReporteRS/generarIngresos',403,token=u['token']))
check('Evaluador no abre ingresos',lambda:expect('ReporteRS/generarIngresos',403,token=evaluator))
def own_list():
 s,b=call('PostulanteRS/listarTodosPostulantes',token=u['token']);assert s==200;rows=json.loads(b);assert len(rows)==1 and rows[0]['id']==u['id'];return {'http':s,'rows':1}
check('Listados filtrados por propietario',own_list)
for i in range(5):call('AutenticacionRS/ingresar','POST',{'correo':u['cred']['correo'],'clave':'Incorrecta para probar bloqueo'})
check('Cinco fallos bloquean contraseña válida',lambda:expect('AutenticacionRS/ingresar',401,'POST',u['cred']))
check('Cerrar sesión revoca token',lambda:expect('AutenticacionRS/salir',204,'POST',token=v['token']))
check('Token revocado no accede',lambda:expect('PostulanteRS/listarTodosPostulantes',401,token=v['token']))
a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n')
print(f'{sum(r["status"]=="PASS" for r in results)}/{len(results)} verificaciones aprobadas; fixtures sintéticas conservadas para auditoría local.')
raise SystemExit(1 if any(r['status']=='FAIL' for r in results)else 0)
