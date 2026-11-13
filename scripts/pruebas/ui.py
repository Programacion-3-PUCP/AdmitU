#!/usr/bin/env python3
"""E2E Blazor/REST con fixtures sintéticas. Requiere Playwright y --apply explícito."""
import argparse,base64,json,re,secrets,sys,time
from datetime import datetime,timedelta
from pathlib import Path
from zoneinfo import ZoneInfo
from regresion import API,PNG,pdf
from playwright.sync_api import sync_playwright,expect
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--apply',action='store_true');p.add_argument('--url',default='http://127.0.0.1:5000');p.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/');p.add_argument('--credentials-file',type=Path,default=Path('work/test-credentials.json'));p.add_argument('--output',type=Path,default=Path('work/evidencias/ui.json'));a=p.parse_args()
if not a.apply:print('DRY-RUN: ningún navegador ni fixture creado.');sys.exit(0)
a.output.parent.mkdir(parents=True,exist_ok=True);api=API(a.base_url);creds=json.loads(a.credentials_file.read_text());admin=api.login(creds['admin']);results=[];tag='UI'+secrets.token_hex(3).upper();today=datetime.now(ZoneInfo('America/Lima')).date()
def j(path,method='GET',data=None,token=admin):return api.json(path,method,data,token)
reg=json.loads(Path('work/evidencias/regresion.json').read_text());conv_id=next(x['detail']['convocatoria'] for x in reg if x['rf']=='RF_01' and x['status']=='PASS');conv=j(f'ConvocatoriaRS/buscarConvocatoriaPorId/{conv_id}');cm=next(x for x in j('ConvocatoriaModalidadRS/listarTodasConvocatoriasModalidad') if x['convocatoria']['id']==conv_id);offer=next(x for x in j('OfertaCarreraRS/listarTodasOfertasCarrera') if x['convocatoria']['id']==conv_id)
user=dict(correo=tag.lower()+'@example.test',clave=secrets.token_urlsafe(24));persona=dict(nombres='Prueba UI',apellidoPaterno=tag,apellidoMaterno='Laboratorio',correo=user['correo'],tipoDocumento='DNI',numeroDocumento=str(10000000+secrets.randbelow(80000000)),telefono='999000001',fechaNacimiento='2005-01-01')
uid=j('AutenticacionRS/registrar','POST',dict(postulante=persona,clave=user['clave']),token='');ut=api.login(user);creds['ui']=dict(user,personaId=uid);a.credentials_file.write_text(json.dumps(creds));a.credentials_file.chmod(0o600)

with sync_playwright() as pw:
 browser=pw.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 contexts={};pages={}
 def login(role,cred,path):
  ctx=browser.new_context(viewport={'width':1440,'height':1000});page=ctx.new_page();page.goto(a.url+'/',wait_until='networkidle');page.locator('#correo').fill(cred['correo']);page.locator('#clave').fill(cred['clave']);page.get_by_role('button',name='Ingresar',exact=True).click();page.wait_for_url('**/'+path);page.wait_for_load_state('networkidle');page.wait_for_timeout(700);contexts[role]=ctx;pages[role]=page;return page
 ad=login('admin',creds['admin'],'admin');us=login('user',user,'postulante');ev=login('evaluador',creds['evaluador'],'evaluador')
 def nav(page,path):
  response=page.goto(a.url+path,wait_until='networkidle');assert response.status==200,(path,response.status);page.wait_for_timeout(1500)
 def check(rf,name,fn,page):
  try:
   detail=fn();results.append(dict(rf=rf,case=name,status='PASS',detail=detail));print('PASS',rf,name,flush=True)
  except Exception as e:
   results.append(dict(rf=rf,case=name,status='FAIL',detail=type(e).__name__+': '+str(e)[:180]));print('FAIL',rf,name,type(e).__name__,str(e)[:180],flush=True)
  finally:
   try:page.screenshot(path=str(a.output.parent/f'ui-{rf}.png'),full_page=True)
   except Exception:pass
   a.output.write_text(json.dumps(results,indent=2,ensure_ascii=False)+'\n')
 def table_search(page,text):page.wait_for_timeout(900);page.locator('input[type=search]').last.fill(text);page.locator('input[type=search]').last.press('Tab');page.wait_for_timeout(400)
 def modalidad():
  nav(ad,'/admin/modalidades');ad.get_by_role('button',name='Nueva modalidad',exact=True).click();expect(ad.locator('#m-codigo')).to_be_visible();ad.locator('#m-codigo').fill(tag);ad.locator('#m-nombre').fill('Modalidad '+tag);ad.locator('#m-desc').fill('Prueba UI');ad.locator('#m-monto').fill('100');ad.locator('.drawer').get_by_role('button',name='Guardar',exact=True).click();expect(ad.locator('.drawer')).to_have_count(0);table_search(ad,tag);row=ad.locator('tbody tr').filter(has_text=tag);expect(row).to_have_count(1)
  row.get_by_role('button',name=re.compile('^Desactivar:')).click();ad.get_by_role('alertdialog').get_by_role('button',name='Desactivar',exact=True).click();expect(ad.get_by_role('alertdialog')).to_have_count(0);row.get_by_role('button',name=re.compile('^Reactivar:')).click();expect(ad.get_by_role('alertdialog')).to_be_visible();ad.get_by_role('alertdialog').get_by_role('button',name='Cancelar',exact=True).click();expect(row).to_contain_text('Inactiva');row.get_by_role('button',name=re.compile('^Reactivar:')).click();ad.get_by_role('alertdialog').get_by_role('button',name='Reactivar',exact=True).click();expect(ad.get_by_role('alertdialog')).to_have_count(0);ad.reload(wait_until='networkidle');ad.wait_for_timeout(900);table_search(ad,tag);expect(ad.locator('tbody tr').filter(has_text=tag)).to_contain_text('Activa');return {'confirmar_cancelar_recarga':True}
 check('RF_04','crear desactivar y confirmar reactivación',modalidad,ad)
 def carrera():
  nav(ad,'/admin/carreras');ad.get_by_role('button',name='Nueva carrera',exact=True).click();expect(ad.locator('#c-codigo')).to_be_visible();options=ad.locator('#c-fac option').evaluate_all('(els)=>els.map(e=>e.value).filter(v=>v!="0")');ad.locator('#c-fac').select_option(options[0]);ad.locator('#c-codigo').fill(tag);ad.locator('#c-nombre').fill('Carrera '+tag);ad.locator('.drawer').get_by_role('button',name='Guardar',exact=True).click();expect(ad.locator('.drawer')).to_have_count(0);ad.reload(wait_until='networkidle');ad.wait_for_timeout(900);table_search(ad,tag);expect(ad.locator('tbody tr').filter(has_text=tag)).to_have_count(1);return {'recarga':True}
 check('RF_06','crear carrera y recuperar al recargar',carrera,ad)
 def convocatoria():
  nav(ad,'/admin/convocatorias');ad.get_by_role('button',name='Nueva convocatoria',exact=True).click();expect(ad.locator('#v-codigo')).to_be_visible();ad.locator('#v-codigo').fill(tag);ad.locator('#v-nombre').fill('Convocatoria '+tag);ad.locator('#v-periodo').fill('2026-2');ad.locator('.drawer').get_by_role('button',name='Guardar',exact=True).click();expect(ad.locator('.drawer')).to_have_count(0);card=ad.locator('article').filter(has_text='Convocatoria '+tag);expect(card).to_have_count(1);card.get_by_role('button',name=re.compile('^Eliminar:')).click();ad.get_by_role('alertdialog').get_by_role('button',name='Eliminar',exact=True).click();expect(ad.get_by_role('alertdialog')).to_have_count(0);ad.reload(wait_until='networkidle');ad.wait_for_timeout(900);expect(ad.locator('article').filter(has_text='Convocatoria '+tag)).to_have_count(0);return {'baja_borrador':True}
 check('RF_01','crear y eliminar borrador con confirmación',convocatoria,ad)
 def requisito():
  nav(ad,'/admin/requisitos');ad.get_by_role('button',name='Nuevo requisito',exact=True).click();
  try:expect(ad.locator('#r-conv')).to_be_visible(timeout=1200)
  except AssertionError:ad.get_by_role('button',name='Nuevo requisito',exact=True).click();expect(ad.locator('#r-conv')).to_be_visible()
  ad.locator('#r-conv').select_option(str(conv_id));ad.locator('#r-mod').select_option(str(cm['modalidad']['id']));ad.locator('#r-nombre').fill('Requisito '+tag);ad.locator('#r-formato').select_option('Pdf');ad.locator('.drawer').get_by_role('button',name='Guardar',exact=True).click();expect(ad.locator('.drawer')).to_have_count(0);matches=[x for x in j('RequisitoRS/listarTodosRequisitos') if x['nombre']=='Requisito '+tag];assert len(matches)==1;return {'requisito':matches[0]['id']}
 check('RF_07','crear requisito en convocatoria y modalidad',requisito,ad)
 def perfil():
  nav(us,'/postulante/perfil');expect(us.locator('#doc')).to_be_disabled();us.locator('#tel').fill('999000003');us.get_by_role('button',name=re.compile('Guardar')).click();us.reload(wait_until='networkidle');us.wait_for_timeout(4000);expect(us.locator('#tel')).to_have_value('999000003');return {'documento_bloqueado':True,'perfil_durable':True}
 check('RF_02','editar perfil y recargar datos persistidos',perfil,us)
 def personales():
  nav(us,'/postulante/documentos-personales')
  for ix in range(2):
   section=us.locator('section.card').nth(ix);section.locator('input[type=file]').set_input_files(dict(name='dni-ui.png' if ix==0 else 'foto-ui.png',mimeType='image/png',buffer=PNG));section.get_by_role('button',name='Guardar',exact=True).click();expect(section.get_by_text('Visualizar / descargar',exact=True)).to_be_visible()
  us.reload(wait_until='networkidle');us.wait_for_timeout(4000);us.get_by_role('button',name='Visualizar / descargar',exact=True).first.click();expect(us.locator('iframe')).to_have_count(1);src=us.locator('iframe').get_attribute('src');assert base64.b64decode(src.split(',',1)[1])==PNG;return {'dos_archivos_durables_y_preview':True}
 check('RF_08','subir DNI foto y recuperar bytes en pantalla',personales,us)
 def certificado():
  nav(us,'/postulante/antecedentes');us.locator('#cert-inst').fill('Universidad '+tag);us.locator('#cert-tipo').select_option('UNIVERSIDAD');us.locator('#cert-inicio').fill('2019');us.locator('#cert-fin').fill('2023');us.locator('input[type=file]').set_input_files(dict(name='certificado-ui.pdf',mimeType='application/pdf',buffer=pdf()));us.get_by_role('button',name='Guardar certificado',exact=True).click();expect(us.get_by_role('button',name='Modificar',exact=True)).to_be_visible();us.reload(wait_until='networkidle');us.wait_for_timeout(4000);us.get_by_role('button',name='Visualizar / descargar',exact=True).click();expect(us.locator('iframe')).to_have_count(1);assert base64.b64decode(us.locator('iframe').get_attribute('src').split(',',1)[1])==pdf();return {'certificado_durable_y_preview':True}
 check('RF_05','certificado PDF carga descarga y recarga',certificado,us)
 def enviar_voucher():
  nav(us,f'/postulante/postular/{conv_id}');us.locator('#carrera').select_option(str(offer['carrera']['id']));us.locator('#modalidad').select_option(str(cm['modalidad']['id']));us.get_by_role('button',name='Continuar',exact=True).click();us.get_by_role('button',name='Continuar',exact=True).click();expect(us.locator('input[type=file]').first).to_be_attached()
  for file in us.locator('input[type=file]').all():file.set_input_files(dict(name='requisito-ui.pdf',mimeType='application/pdf',buffer=pdf()))
  us.get_by_role('button',name='Continuar',exact=True).click();us.locator('input[type=checkbox]').check();us.get_by_role('button',name='Enviar postulación',exact=True).click();us.wait_for_url('**/postulante/vouchers');us.get_by_role('button',name='Registrar voucher',exact=True).click();us.locator('#voucher-operacion').fill('OP-'+tag);us.locator('input[type=file]').set_input_files(dict(name='voucher-ui.png',mimeType='image/png',buffer=PNG));us.get_by_role('button',name='Enviar voucher',exact=True).click();expect(us.get_by_text('PENDIENTE',exact=False).first).to_be_visible();rows=j('PagoRS/listarTodosPagos',token=ut);assert len(rows)==1 and rows[0]['estadoPago']=='PENDIENTE';posts=j('PostulacionRS/listarTodasPostulaciones',token=ut);assert len(posts)==1;globals()['post']=posts[0];globals()['payment']=rows[0];return {'postulacion':post['id'],'un_solo_pago':payment['id']}
 check('RF_11','wizard enviar documentos y registrar voucher',enviar_voucher,us)
 if 'post' in globals():
  def validar():
   nav(ev,'/evaluador/vouchers');table_search(ev,'OP-'+tag);row=ev.locator('tbody tr').filter(has_text='OP-'+tag);row.get_by_role('button',name=re.compile('^Aprobar:')).click();ev.get_by_role('alertdialog').get_by_role('button',name='Validar pago',exact=True).click();expect(ev.get_by_role('alertdialog')).to_have_count(0);assert j(f'PagoRS/buscarPagoPorId/{payment["id"]}')['estadoPago']=='APROBADO';return {'aprobado':True}
  check('RF_12','evaluador aprueba voucher desde pantalla',validar,ev)
  def observar():
   nav(ev,'/evaluador/observaciones');ev.get_by_role('button',name='Nueva observación',exact=True).click();expect(ev.locator('#o-conv')).to_be_visible();ev.locator('#o-conv').select_option(str(conv_id));ev.locator('#o-post').select_option(str(post['id']));ev.locator('#o-desc').fill('Documento ilegible '+tag);ev.locator('.drawer').get_by_role('button',name='Guardar',exact=True).click();expect(ev.locator('.drawer')).to_have_count(0);ev.locator('#alcance-observaciones').select_option('mios');table_search(ev,tag);expect(ev.locator('tbody tr').filter(has_text=tag)).to_have_count(1);ev.locator('input[type=search]').last.fill('');ev.locator('#alcance-observaciones').select_option('mios');ev.wait_for_timeout(500);mine=int(ev.locator('.table-count').inner_text().split()[0]);ev.locator('#alcance-observaciones').select_option('todos');ev.wait_for_timeout(500);total=int(ev.locator('.table-count').inner_text().split()[0]);assert total>mine;expect(ev.locator('#alcance-observaciones option:checked')).to_have_text('Todos los expedientes');return {'filtro_visible_funcional':True,'mis_expedientes':mine,'todos':total}
  check('RF_10','observar documento y usar alcance visible',observar,ev)
 def reporte(page,path):
  nav(page,path);page.locator('#rp-conv').select_option(str(conv_id));page.get_by_role('button',name='Generar reporte',exact=True).click();expect(page.locator('iframe')).to_have_count(1);expect(page.get_by_role('link',name='Descargar PDF',exact=True)).to_be_visible();data=base64.b64decode(page.locator('iframe').get_attribute('src').split(',',1)[1]);assert data.startswith(b'%PDF-');return {'pdf_bytes':len(data)}
 check('RF_03','Jasper ingresos previsualizar y descargar',lambda:reporte(ad,'/admin/reportes/ingresos'),ad)
 check('RF_09','Jasper observados previsualizar y descargar',lambda:reporte(ev,'/evaluador/reportes/observados'),ev)
 check('RF_15','Jasper vacantes previsualizar y descargar',lambda:reporte(ad,'/admin/reportes/vacantes'),ad)
 if 'post' in globals():
  def historial():
   nav(ad,'/admin/historial-estados');ad.get_by_role('combobox',name='Convocatoria',exact=True).select_option(str(conv_id));table_search(ad,post['codigoInscripcion']);expect(ad.locator('tbody tr').first).to_contain_text(post['codigoInscripcion']);return {'historial_visible':True}
  check('RF_13','consultar historial real del trámite UI',historial,ad)
  def carne():
   # La preparación API aprueba los documentos; la emisión/anulación se ejerce en la UI.
   docs=j('DocumentoPostulacionRS/listarTodosDocumentosPostulacion',token=ut)
   for doc in docs:doc['estadoDocumento']='APROBADO';j('DocumentoPostulacionRS/modificarDocumentoPostulacion','PUT',doc)
   d=j(f'PostulacionRS/buscarPostulacionPorId/{post["id"]}');d['estadoActual']={'id':5};j('PostulacionRS/modificarPostulacion','PUT',d)
   nav(ad,'/admin/carnes');ad.get_by_role('button',name=re.compile('Aptos sin carné')).click();table_search(ad,post['codigoInscripcion']);ad.get_by_role('button',name=re.compile('^Emitir carné:')).click();ad.get_by_role('alertdialog').get_by_role('button',name='Emitir',exact=True).click();expect(ad.get_by_role('alertdialog')).to_have_count(0);cards=[x for x in j('CarnePostulanteRS/listarTodosCarnesPostulante') if x['postulacion']['id']==post['id']];assert len(cards)==1;assert j(f'PostulacionRS/buscarPostulacionPorId/{post["id"]}')['estadoActual']['id']==6;nav(us,'/postulante/mi-postulacion');expect(us.locator('main')).to_contain_text('Finalizada');return {'carne_emitido':cards[0]['id'],'estado_SQL_y_Blazor_finalizada':True}
  check('RF_14','emitir carné apto desde administración',carne,ad)
 browser.close()
print('RESULTADO',sum(x['status']=='PASS' for x in results),'/',len(results));sys.exit(1 if any(x['status']=='FAIL' for x in results) else 0)
