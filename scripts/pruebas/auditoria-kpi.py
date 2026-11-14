#!/usr/bin/env python3
"""Contraste de indicadores SQL -> REST -> resumen Blazor; solo lectura de negocio."""
import argparse
import json
from decimal import Decimal
from pathlib import Path
import re
import subprocess
import sys
from regresion import API

SQL = """
WITH indicadores AS (
 SELECT p.id,p.convocatoria_id,e.codigo,p.fecha_envio,
 NOT EXISTS(SELECT 1 FROM requisito_convocatoria_modalidad rm
  WHERE rm.convocatoria_modalidad_id=p.modalidad_elegida_id AND rm.obligatorio=1
  AND NOT EXISTS(SELECT 1 FROM documento_postulacion d JOIN archivo_digital a
   ON a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id
   AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0
   WHERE d.postulacion_id=p.id AND d.requisito_aplicable_id=rm.id AND d.estado_documento='APROBADO')) docs,
 EXISTS(SELECT 1 FROM pago pg WHERE pg.postulacion_id=p.id AND pg.estado_pago='APROBADO') pago,
 EXISTS(SELECT 1 FROM carne_postulante cp WHERE cp.postulacion_id=p.id
  AND (cp.aula_examen IS NULL OR cp.aula_examen NOT LIKE 'ANULADO%')) carne
 FROM postulacion p JOIN estado_postulacion e ON e.id=p.estado_actual_id)
SELECT c.id,COUNT(i.id),
 COALESCE(SUM(i.codigo<>'BORRADOR' AND i.fecha_envio IS NOT NULL),0),
 COALESCE(SUM(i.codigo<>'BORRADOR' AND i.fecha_envio IS NOT NULL AND i.pago),0),
 COALESCE(SUM(i.codigo<>'BORRADOR' AND i.fecha_envio IS NOT NULL AND i.docs),0),
 COALESCE(SUM(i.codigo IN('APTA','FINALIZADA','ADMITIDA') AND i.fecha_envio IS NOT NULL AND i.docs AND i.pago),0),
 COALESCE(SUM(i.codigo<>'BORRADOR' AND i.fecha_envio IS NOT NULL AND i.carne),0),
 COALESCE(SUM(i.codigo IN('APTA','FINALIZADA','ADMITIDA') AND (NOT i.docs OR NOT i.pago)),0)
 FROM convocatoria c LEFT JOIN indicadores i ON i.convocatoria_id=c.id
 GROUP BY c.id ORDER BY c.id;
"""
LABELS = ['Registradas','Enviadas','Pago validado','Documentos completos','Expediente apto','Con carné']

def cli():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--credentials-file',type=Path,required=True)
    p.add_argument('--mysql-defaults-file',type=Path,required=True)
    p.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/')
    p.add_argument('--url',default='http://127.0.0.1:5000')
    p.add_argument('--skip-ui',action='store_true')
    p.add_argument('--output',type=Path,default=Path('work/evidencias/auditoria-kpi-sql.json'))
    return p.parse_args()

def main():
    a=cli();result={'environment':'Codex Cloud propio; no WSL/AWS','business_writes':False,'cases':[]}
    def check(name,fn):
        try:
            detail=fn();result['cases'].append(dict(case=name,status='PASS',detail=detail));print('PASS',name,flush=True)
        except Exception as e:
            result['cases'].append(dict(case=name,status='FAIL',detail=str(e)[:200]));print('FAIL',name,type(e).__name__,str(e)[:150],flush=True)
    def sql(text):
        return subprocess.check_output(['mysql','--defaults-extra-file='+str(a.mysql_defaults_file.resolve()),'--default-character-set=utf8mb4','-N','admitu_db','-e',text],text=True)
    raw=sql(SQL)
    expected={int(row[0]):[int(x) for x in row[1:]] for row in (line.split('\t') for line in raw.splitlines())}
    revenue={int(r[0]):Decimal(r[1]) for r in (line.split('\t') for line in sql("SELECT c.id,COALESCE(SUM(IF(p.fecha_envio IS NOT NULL AND p.estado_actual_id<>1 AND pg.estado_pago='APROBADO',pg.monto,0)),0) FROM convocatoria c LEFT JOIN postulacion p ON p.convocatoria_id=c.id LEFT JOIN pago pg ON pg.postulacion_id=p.id GROUP BY c.id").splitlines())}
    api=API(a.base_url);creds=json.loads(a.credentials_file.read_text());token=api.login(creds['admin'])
    def get(path):return api.json(path,token=token)
    try:
        posts=get('PostulacionRS/listarTodasPostulaciones');docs=get('DocumentoPostulacionRS/listarTodosDocumentosPostulacion')
        payments=get('PagoRS/listarTodosPagos');cards=get('CarnePostulanteRS/listarTodosCarnesPostulante')
        associations=get('RequisitoConvocatoriaModalidadRS/listarTodosRequisitosConvocatoriaModalidad')
        api_counts={cid:[0]*7 for cid in expected}
        for p in posts:
            cid=p['convocatoria']['id'];state_id=(p.get('estadoActual') or {}).get('id');state={1:'BORRADOR',2:'PEND_PAGO',3:'EN_REVISION',4:'OBSERVADA',5:'APTA',6:'FINALIZADA',7:'RECHAZADA',8:'ANULADA'}.get(state_id)
            sent=state!='BORRADOR' and bool(p.get('fechaEnvio'))
            reqs={r['id'] for r in associations if r.get('obligatorio') and (r.get('convocatoriaModalidad') or {}).get('id')==p['modalidadElegida']['id']}
            approved={d['requisitoAplicable']['id'] for d in docs if d['postulacion']['id']==p['id'] and d.get('estadoDocumento')=='APROBADO' and d.get('archivoActivo')}
            complete=reqs<=approved
            paid=any(pg['postulacion']['id']==p['id'] and pg['estadoPago']=='APROBADO' for pg in payments)
            card=any(c['postulacion']['id']==p['id'] and not (c.get('aulaExamen') or '').startswith('ANULADO') for c in cards)
            favorable=state in ('APTA','FINALIZADA','ADMITIDA')
            values=[True,sent,sent and paid,sent and complete,sent and favorable and complete and paid,sent and card,favorable and not(complete and paid)]
            for i,v in enumerate(values):api_counts[cid][i]+=bool(v)
        for cid,counts in expected.items():
            def compare(cid=cid,counts=counts):
                assert api_counts[cid]==counts,f'conv {cid}: SQL={counts} REST={api_counts[cid]}'
                return dict(convocatoria=cid,sql=counts,rest=api_counts[cid],columns=LABELS+['Estados favorables inconsistentes'])
            check('SQL/REST convocatoria '+str(cid),compare)
        check('ArchivoActivo viene de SQL para todos los documentos',lambda:metadata(docs,sql))
        if not a.skip_ui:
            from playwright.sync_api import sync_playwright,expect
            with sync_playwright() as pw:
                browser=pw.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
                context=browser.new_context(viewport={'width':1440,'height':1000})
                context.route('https://fonts.googleapis.com/**',lambda r:r.abort());context.route('https://fonts.gstatic.com/**',lambda r:r.abort())
                page=context.new_page();page.set_default_timeout(60000)
                page.goto(a.url+'/',wait_until='domcontentloaded')
                page.locator('#correo').fill(creds['admin']['correo']);page.locator('#clave').fill(creds['admin']['clave'])
                page.get_by_role('button',name='Ingresar',exact=True).click();page.wait_for_url(a.url+'/admin',wait_until='domcontentloaded')
                expect(page.locator('.funnel')).to_be_visible()
                page.wait_for_timeout(1000)
                ui_ids=[int(x) for x in page.locator('#conv option').evaluate_all('(els)=>els.map(e=>e.value)')]
                for cid in ui_ids:
                    page.locator('#conv').select_option(str(cid));expect(page.locator('.funnel-row').first).to_have_attribute('href','/admin/postulaciones?conv='+str(cid))
                    def compare_ui(cid=cid):
                        actual=[int(x) for x in page.locator('.funnel-row strong').all_text_contents()]
                        assert actual==expected[cid][:6],f'conv {cid}: SQL={expected[cid][:6]} UI={actual}'
                        cards={row.locator('.stat-label').inner_text():row.locator('.stat-value').inner_text() for row in page.locator('.kpis .stat').all()}
                        assert int(cards['Postulaciones enviadas'])==expected[cid][1]
                        assert int(cards['Expedientes aptos'])==expected[cid][4]
                        assert int(cards['Carnés emitidos'])==expected[cid][5]
                        actual_revenue=Decimal(re.sub(r'[^0-9.]','',cards['Recaudado']))
                        assert actual_revenue==revenue[cid],f'Recaudación UI={actual_revenue} SQL={revenue[cid]}'
                        assert all(not x.strip() for x in page.locator('.funnel-rate').all_text_contents()),'Persisten conversiones entre cohortes'
                        notes=page.locator('div.inline-note[role=status]').all_text_contents()
                        if expected[cid][6]>0:assert any(x.strip().startswith(str(expected[cid][6])+' expedientes') for x in notes),'No se muestra incidencia histórica'
                        else:assert not any('estado favorable' in x for x in notes)
                        return dict(convocatoria=cid,sql=expected[cid][:6],ui=actual,recaudacion_sql=str(revenue[cid]),recaudacion_ui=str(actual_revenue),independent_metrics=True,inconsistent_states_shown=expected[cid][6])
                    check('SQL/Chromium convocatoria '+str(cid),compare_ui)
                def assignment():
                    assigned=[p for p in posts if p.get('evaluadorId') and p.get('fechaEnvio') and (p.get('estadoActual') or {}).get('id')!=1]
                    ids=sorted({p['convocatoria']['id'] for p in assigned if p['convocatoria']['id'] in ui_ids})
                    for cid in ids:
                        page.locator('#conv').select_option(str(cid));expect(page.locator('.funnel-row').first).to_have_attribute('href','/admin/postulaciones?conv='+str(cid))
                        table=page.locator('section').filter(has=page.get_by_role('heading',name='Carga de evaluadores',exact=True)).locator('table')
                        observed={int(re.search(r'evaluador=(\d+)',row.locator('td a').get_attribute('href')).group(1)):int(row.locator('td').nth(1).inner_text()) for row in table.locator('tbody tr').all()}
                        for eid,total in observed.items():
                            count=sum(p['convocatoria']['id']==cid and p['evaluadorId']==eid for p in assigned)
                            assert count==total,f'asignación convocatoria {cid}, evaluador {eid}: {total}!={count}'
                    return dict(convocatorias_con_asignados=ids,assignment_read_back=True)
                check('Carga de evaluadores coincide con asignación persistida',assignment)
                page.screenshot(path='work/evidencias/auditoria-kpi.png',full_page=True)
                browser.close()
    finally:
        api.call('AutenticacionRS/salir','POST',token=token)
        a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
    return 1 if any(c['status']=='FAIL' for c in result['cases']) else 0

def metadata(docs,sql):
    rows=sql("SELECT d.id,EXISTS(SELECT 1 FROM archivo_digital a JOIN postulacion p ON p.id=d.postulacion_id WHERE a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0) FROM documento_postulacion d ORDER BY d.id")
    values={int(r[0]):bool(int(r[1])) for r in (line.split('\t') for line in rows.splitlines())}
    assert len(docs)==len(values)
    assert all(d.get('archivoActivo')==values[d['id']] for d in docs)
    return dict(documentos=len(docs),con_bytes=sum(values.values()),without_client_flags=True)

if __name__=='__main__':sys.exit(main())
