#!/usr/bin/env python3
"""Registro de postulante por Blazor y acceso posterior por el formulario nativo de cookies."""
import argparse
import json
import re
import secrets
from pathlib import Path
from playwright.sync_api import sync_playwright, expect

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--apply', action='store_true')
parser.add_argument('--url', default='http://127.0.0.1:5000')
parser.add_argument('--output', type=Path, default=Path('work/evidencias/registro-ui.json'))
args = parser.parse_args()
if not args.apply:
    print('DRY-RUN: use --apply para registrar una cuenta sintética en el ensayo autorizado.')
    raise SystemExit(0)
tag = secrets.token_hex(5)
correo = 'registro-ui-' + tag + '@example.test'
clave = secrets.token_urlsafe(24)
documento = str(10000000 + secrets.randbelow(80000000))
step = 'abrir_registro'
try:
    with sync_playwright() as pw:
        browser = pw.chromium.launch(executable_path='/usr/bin/chromium', headless=True, args=['--no-sandbox'])
        context = browser.new_context(viewport={'width': 1440, 'height': 1000})
        context.route('https://fonts.googleapis.com/**', lambda route: route.abort())
        context.route('https://fonts.gstatic.com/**', lambda route: route.abort())
        step = 'formulario_ssr_protegido'
        ssr = context.request.get(args.url + '/registro')
        assert ssr.status == 200 and re.search(r'<fieldset\b[^>]*\bdisabled(?:[ >]|=)', ssr.text())
        page = context.new_page()
        page.goto(args.url + '/', wait_until='networkidle')
        page.get_by_role('link', name='Crear cuenta', exact=True).click()
        page.wait_for_url(args.url + '/registro')
        page.wait_for_load_state('networkidle')
        expect(page.locator('#r-nac')).to_have_value('')
        page.locator('#r-nombres').fill('Prueba Registro')
        page.locator('#r-pat').fill('UI' + tag)
        page.locator('#r-mat').fill('Laboratorio')
        page.locator('#r-nac').fill('2005-01-01')
        page.locator('#r-tipo').select_option('DNI')
        page.locator('#r-num').fill(documento)
        page.locator('#r-correo').fill(correo)
        page.locator('#r-clave').fill(clave)
        step = 'enviar_formulario'
        page.get_by_role('button', name='Crear cuenta', exact=True).click()
        step = 'confirmar_cuenta'
        expect(page.get_by_text(re.compile('Tu cuenta fue creada'))).to_be_visible()
        step = 'volver_login'
        page.get_by_role('link', name='Ir a ingresar', exact=True).click()
        page.wait_for_url(args.url + '/')
        page.locator('#correo').fill(correo)
        page.locator('#clave').fill(clave)
        page.get_by_role('button', name='Ingresar', exact=True).click()
        step = 'ingresar_postulante'
        page.wait_for_url(args.url + '/postulante')
        step = 'verificar_perfil'
        page.goto(args.url + '/postulante/perfil', wait_until='networkidle')
        expect(page.locator('#doc')).to_have_value(documento)
        expect(page.locator('#doc')).to_be_disabled()
        expect(page.locator('#nac')).to_have_value('2005-01-01')
        assert not page.locator('#blazor-error-ui').is_visible()
        cookies = [cookie for cookie in context.cookies() if cookie['name'] == 'AdmitU.Sesion']
        assert len(cookies) == 1 and cookies[0]['httpOnly'] and cookies[0]['sameSite'] == 'Strict'
        args.output.parent.mkdir(parents=True, exist_ok=True)
        page.screenshot(path=str(args.output.parent / 'registro-ui.png'), full_page=True)
        browser.close()
    result = dict(entorno='Codex Cloud; Chromium y REST/MySQL reales; no WSL ni AWS', rf='RF_02', status='PASS',
                  registro_interactivo=True, formulario_espera_interactividad=True, nacimiento_sin_prellenado=True, regreso_login_estatico=True,
                  cookie_http_only=True, cookie_same_site='Strict', perfil_persistido=True, documento_bloqueado=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(result, ensure_ascii=False))
except Exception as error:
    args.output.parent.mkdir(parents=True, exist_ok=True)
    result = dict(entorno='Codex Cloud; no WSL ni AWS', rf='RF_02', status='FAIL', error=type(error).__name__, step=step)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(result, ensure_ascii=False))
    raise SystemExit(1)
