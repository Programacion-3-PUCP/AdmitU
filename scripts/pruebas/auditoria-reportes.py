#!/usr/bin/env python3
"""PDF Jasper auténtico por REST, permisos y contraste SQL; no modifica negocio."""
import argparse
import json
from decimal import Decimal
from pathlib import Path
import re
import subprocess
import sys
from urllib.parse import urlencode
from regresion import API

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--credentials-file',type=Path,required=True)
    p.add_argument('--mysql-defaults-file',type=Path,required=True)
    p.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/')
    p.add_argument('--output',type=Path,default=Path('work/evidencias/auditoria-reportes.json'))
    a=p.parse_args();api=API(a.base_url);creds=json.loads(a.credentials_file.read_text())
    results={'environment':'Codex Cloud propio; no WSL/AWS','business_writes':False,'cases':[]}
    tokens={r:api.login(creds[r]) for r in ('admin','evaluador','postulante')}
    private=Path('work/evidencias/auditoria-reportes');private.mkdir(parents=True,exist_ok=True)
    def check(name,fn):
        try:
            detail=fn();results['cases'].append(dict(case=name,status='PASS',detail=detail));print('PASS',name,flush=True)
        except Exception as e:
            results['cases'].append(dict(case=name,status='FAIL',detail=str(e)[:200]));print('FAIL',name,type(e).__name__,str(e)[:120],flush=True)
    def sql(query):
        raw=subprocess.check_output(['mysql','--defaults-extra-file='+str(a.mysql_defaults_file.resolve()),'--default-character-set=utf8mb4','-N','admitu_db','-e',query],text=True)
        count,money=raw.strip().split('\t');return int(count),Decimal(money)
    def contrast(kind,filters):
        conditions=[]
        if filters.get('convocatoriaId'):conditions.append('p.convocatoria_id='+str(int(filters['convocatoriaId'])))
        if filters.get('modalidadId'):conditions.append('cm.modalidad_id='+str(int(filters['modalidadId'])))
        datecolumn={'Ingresos':'pg.fecha_pago','Observados':'o.fecha_observacion','Vacantes':'p.fecha_finalizacion'}[kind]
        for key,op in [('desde','>='),('hasta','<=')]:
            if key in filters:conditions.append(datecolumn+op+"'"+filters[key]+"'")
        extra=' AND '+' AND '.join(conditions) if conditions else ''
        join=' JOIN convocatoria_modalidad cm ON cm.id=p.modalidad_elegida_id '
        if kind=='Ingresos':q="SELECT COUNT(*),COALESCE(SUM(pg.monto),0) FROM pago pg JOIN postulacion p ON p.id=pg.postulacion_id"+join+"WHERE pg.estado_pago='APROBADO'"+extra
        elif kind=='Observados':q="SELECT COUNT(*),0 FROM documento_observacion o JOIN documento_postulacion d ON d.id=o.documento_id JOIN postulacion p ON p.id=d.postulacion_id"+join+"WHERE o.estado_observacion='PENDIENTE'"+extra
        else:q="SELECT COUNT(*),0 FROM postulacion p JOIN estado_postulacion e ON e.id=p.estado_actual_id"+join+"WHERE e.codigo='FINALIZADA'"+extra
        before=sql(q)
        route='ReporteRS/generar'+kind+('?' +urlencode(filters) if filters else '')
        status,headers,data=api.call(route,token=tokens['admin'])
        assert status==200,f'HTTP{status}'
        assert headers.get('Content-Type','').startswith('application/pdf') and data.startswith(b'%PDF-') and len(data)>1000
        assert headers.get('Cache-Control')=='no-store'
        name=kind+'-'+str(len(results['cases']));file=private/(name+'.pdf');file.write_bytes(data)
        text=subprocess.check_output(['pdftotext','-layout',str(file),'-'],text=True)
        after=sql(q);assert before==after,'Los datos cambiaron durante lectura; repetir sin escrituras concurrentes'
        n,m=before
        if kind=='Ingresos':assert re.search(r'Total validado S/\s+'+re.escape(f'{m:,.2f}')+r'\b',text),'Total PDF difiere SQL'
        elif kind=='Observados':assert re.search(r'Observaciones pendientes:\s+'+str(n)+r'\b',text),'Observaciones PDF difieren SQL'
        else:
            assert re.search(r'Total completadas\s+'+str(n)+r'\b',text),'FINALIZADA PDF difiere SQL'
            assert 'compartido' in text,'Falta alcance de cupo compartido'
        return dict(http=status,pdf_bytes=len(data),filters=filters,sql_count=n,sql_sum=str(m),pdf_sql_match=True)
    try:
        for role in tokens:
            for kind in ('Ingresos','Observados','Vacantes'):
                def permission(role=role,kind=kind):
                    expected=200 if role=='admin' or (role=='evaluador' and kind=='Observados') else 403
                    status,headers,data=api.call('ReporteRS/generar'+kind,token=tokens[role]);assert status==expected,f'HTTP{status} esperado{expected}'
                    if expected==200:assert data.startswith(b'%PDF-') and headers.get('Content-Type','').startswith('application/pdf')
                    return dict(role=role,report=kind,http=status)
                check('Permiso '+role+'/'+kind,permission)
        for kind in ('Ingresos','Observados','Vacantes'):
            for filters in ({},{'convocatoriaId':1,'modalidadId':1},{'convocatoriaId':999999},{'desde':'2026-01-01','hasta':'2026-12-31'}):
                check('PDF/SQL '+kind+' '+str(filters),lambda kind=kind,filters=filters:contrast(kind,filters))
            for query in ('convocatoriaId=-1','desde=2026-12-31&hasta=2026-01-01','desde=fecha-invalida'):
                def invalid(kind=kind,query=query):
                    status,_,_=api.call('ReporteRS/generar'+kind+'?'+query,token=tokens['admin']);assert status==400,f'HTTP{status} esperado400';return dict(http=status)
                check('Filtro inválido '+kind+' '+query,invalid)
            def anonymous(kind=kind):
                status,_,_=api.call('ReporteRS/generar'+kind);assert status==401;return dict(http=status)
            check('Sin sesión '+kind,anonymous)
    finally:
        for token in tokens.values():api.call('AutenticacionRS/salir','POST',token=token)
        a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n')
    return 1 if any(x['status']=='FAIL' for x in results['cases']) else 0

if __name__=='__main__':sys.exit(main())
