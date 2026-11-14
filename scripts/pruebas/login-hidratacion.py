#!/usr/bin/env python3
"""Regresión Chromium: el formulario de ingreso conserva lo escrito durante la hidratación."""
import argparse
import json
from pathlib import Path
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--credentials-file', type=Path, required=True)
parser.add_argument('--role', choices=['admin', 'evaluador'], default='evaluador')
parser.add_argument('--url', default='http://127.0.0.1:5000')
parser.add_argument('--output', type=Path, default=Path('work/evidencias/login-hidratacion.json'))
args = parser.parse_args()
credential = json.loads(args.credentials_file.read_text())[args.role]
cases = []
with sync_playwright() as pw:
    browser = pw.chromium.launch(executable_path='/usr/bin/chromium', headless=True, args=['--no-sandbox'])
    for mode in ['domcontentloaded', 'networkidle']:
        events = []
        context = browser.new_context()
        context.route('https://fonts.googleapis.com/**', lambda route: route.abort())
        context.route('https://fonts.gstatic.com/**', lambda route: route.abort())
        page = context.new_page()
        def record(response):
            if urlsplit(response.url).path == '/cuenta/ingresar':
                events.append(dict(status=response.status, path='/cuenta/ingresar', location=response.headers.get('location', '')))
        page.on('response', record)
        page.goto(args.url + '/', wait_until=mode)
        page.locator('#correo').fill(credential['correo'])
        page.locator('#clave').fill(credential['clave'])
        filled = (page.locator('#correo').input_value() == credential['correo']
                  and page.locator('#clave').input_value() == credential['clave'])
        page.get_by_role('button', name='Ingresar', exact=True).click()
        try:
            page.wait_for_url(args.url + '/' + args.role, wait_until='domcontentloaded', timeout=15000)
            ok = True
        except PlaywrightTimeout:
            ok = False
        cases.append(dict(mode=mode, filled_before_submit=filled, ok=ok, path=urlsplit(page.url).path, login_requests=events))
        context.close()
    browser.close()
result = dict(entorno='Codex Cloud; Chromium real; no WSL ni AWS', role=args.role, cases=cases)
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
print(json.dumps(result, ensure_ascii=False))
raise SystemExit(0 if all(case['ok'] and case['filled_before_submit'] for case in cases) else 1)
