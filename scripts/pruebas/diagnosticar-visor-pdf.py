#!/usr/bin/env python3
"""Aísla un bloqueo del navegador de un PDF inválido; solo HTTP loopback y archivos privados."""
import argparse
import base64
import http.server
import json
import threading
from pathlib import Path
from playwright.sync_api import sync_playwright


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--pdf', type=Path, required=True)
    parser.add_argument('--output', type=Path, default=Path('work/evidencias/visor-pdf-diagnostico.json'))
    args = parser.parse_args()
    contenido = args.pdf.read_bytes()
    if not contenido.startswith(b'%PDF-'):
        parser.error('El archivo no comienza con la firma PDF.')
    args.output.parent.mkdir(parents=True, exist_ok=True)

    class Servidor(http.server.BaseHTTPRequestHandler):
        def do_GET(self):
            pdf = self.path == '/prueba.pdf'
            data = contenido if pdf else b'<!doctype html><title>Diagnostico PDF local</title><h1>PDF local</h1><iframe width="1000" height="800"></iframe>'
            self.send_response(200)
            self.send_header('Content-Type', 'application/pdf' if pdf else 'text/html; charset=utf-8')
            self.send_header('Content-Length', str(len(data)))
            self.end_headers()
            self.wfile.write(data)

        def log_message(self, *_):
            pass

    servidor = http.server.ThreadingHTTPServer(('127.0.0.1', 0), Servidor)
    threading.Thread(target=servidor.serve_forever, daemon=True).start()
    base = 'http://127.0.0.1:' + str(servidor.server_port)
    resultados = []
    try:
        with sync_playwright() as pw:
            browser = pw.chromium.launch(executable_path='/usr/bin/chromium', headless=True, args=['--no-sandbox'])
            page = browser.new_page(viewport={'width': 1100, 'height': 950})
            for caso, uri in [
                ('pdf_data_uri', 'data:application/pdf;base64,' + base64.b64encode(contenido).decode()),
                ('html_data_uri', 'data:text/html,<h1>Control de esquema data</h1>'),
                ('pdf_http_loopback', base + '/prueba.pdf'),
            ]:
                page.goto(base, wait_until='domcontentloaded')
                page.locator('iframe').evaluate('(e,u)=>e.src=u', uri)
                page.wait_for_timeout(3000)
                frames = page.frames[1:]
                error = next((f.locator('body').inner_text()[:150] for f in frames if f.url.startswith('chrome-error:')), None)
                resultados.append({
                    'caso': caso,
                    'pdf_viewer_habilitado': page.evaluate('navigator.pdfViewerEnabled'),
                    'esquemas_frames': [f.url.split(':', 1)[0] for f in frames],
                    'error_navegador': error,
                    'componente_pdf_cargado': any(f.url.startswith('chrome-extension://mhjfbmdgcfjbbpaeojofohoefgiehjai/') for f in frames),
                })
                page.screenshot(path=str(args.output.parent / (args.output.stem + '-' + caso + '.png')), full_page=True)
            browser.close()
    finally:
        servidor.shutdown()
        servidor.server_close()
    resultado = {
        'entorno': 'Codex Cloud; no WSL ni AWS',
        'pdf_bytes': len(contenido),
        'alcance': 'PDF y HTML data URI contrastados con los mismos bytes PDF sobre HTTP loopback; no cambia políticas ni la aplicación',
        'casos': resultados,
    }
    args.output.write_text(json.dumps(resultado, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(resultado, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
