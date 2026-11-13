#!/usr/bin/env python3
"""Prueba de cookies/roles con Chromium real y dos sesiones independientes. Claves privadas."""
import argparse,json
from pathlib import Path
from playwright.sync_api import sync_playwright
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--base-url',default='http://127.0.0.1:5000')
p.add_argument('--credentials-file',type=Path,required=True)
p.add_argument('--output',type=Path,default=Path('work/seguridad-ui.json'))
a=p.parse_args();creds=json.loads(a.credentials_file.read_text());results=[]
def check(name,fn):
 try:
  detail=fn();results.append({'case':name,'status':'PASS','detail':detail});print('PASS',name,flush=True)
 except Exception as e:results.append({'case':name,'status':'FAIL','detail':type(e).__name__});print('FAIL',name,type(e).__name__,flush=True)
with sync_playwright() as pw:
 browser=pw.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 contexts={};pages={}
 for role in ('admin','evaluador'):
  context=browser.new_context(viewport={'width':1440,'height':1000});context.route('https://fonts.googleapis.com/**',lambda r:r.abort());context.route('https://fonts.gstatic.com/**',lambda r:r.abort())
  contexts[role]=context;pages[role]=context.new_page()
 def csrf():
  r=contexts['admin'].request.post(a.base_url+'/cuenta/ingresar',form={'correo':creds['admin']['correo'],'clave':creds['admin']['clave']});assert r.status==400;return {'http':r.status}
 check('Login rechaza POST sin antiforgery',csrf)
 def login(role):
  page=pages[role];page.goto(a.base_url+'/',wait_until='domcontentloaded')
  page.locator('input[name=correo]').fill(creds[role]['correo']);page.locator('input[name=clave]').fill(creds[role]['clave'])
  page.locator('button[type=submit]').click();page.wait_for_url(a.base_url+'/'+role,wait_until='domcontentloaded');page.locator('h1').first.wait_for()
  assert page.locator('#blazor-error-ui').is_visible() is False
  assert 'No pudimos cargar' not in page.locator('body').inner_text()
  cookies=[c for c in contexts[role].cookies() if c['name']=='AdmitU.Sesion'];assert len(cookies)==1 and cookies[0]['httpOnly'] and cookies[0]['sameSite']=='Strict'
  return {'route':'/'+role,'httpOnly':True,'sameSite':'Strict'}
 for role in ('admin','evaluador'):check('Login real y cookie '+role,lambda role=role:login(role))
 def isolation():
  page=pages['admin'];r=page.goto(a.base_url+'/admin',wait_until='domcontentloaded');assert r and r.status==200;assert 'Acceso restringido' not in page.locator('body').inner_text();return {'admin_session_preserved':True}
 check('Sesión admin permanece independiente del evaluador',isolation)
 def forbid():
  page=pages['evaluador'];r=page.goto(a.base_url+'/admin',wait_until='domcontentloaded');text=page.locator('body').inner_text();assert (r and r.status in (401,403)) or 'Acceso restringido' in text or page.url.endswith('/acceso-denegado');return {'role_boundary':True}
 check('Evaluador no puede abrir área admin',forbid)
 def logout():
  page=pages['admin'];page.goto(a.base_url+'/admin',wait_until='domcontentloaded');page.locator('button[aria-label="Cerrar sesión"]').click();page.wait_for_url(a.base_url+'/',wait_until='domcontentloaded');assert not any(c['name']=='AdmitU.Sesion'for c in contexts['admin'].cookies());return {'cookie_removed':True}
 check('Cerrar sesión elimina cookie',logout)
 browser.close()
a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n')
print(f'{sum(r["status"]=="PASS"for r in results)}/{len(results)} verificaciones de navegador aprobadas.')
raise SystemExit(1 if any(r['status']=='FAIL'for r in results)else 0)
