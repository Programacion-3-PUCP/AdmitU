#!/usr/bin/env python3
"""Portal evaluador: resumen global y detalles asignados; usa fixtures privados del clon."""
import argparse
import json
import re
from pathlib import Path
from playwright.sync_api import sync_playwright, expect

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--url', default='http://127.0.0.1:5000')
parser.add_argument('--credentials-file', type=Path, required=True)
parser.add_argument('--fixtures-manifest', type=Path, required=True)
parser.add_argument('--output', type=Path, default=Path('work/evidencias/privacidad-ui.json'))
args = parser.parse_args()
if not args.url.startswith(('http://127.0.0.1:', 'http://localhost:')):
    parser.error('Esta prueba está limitada al portal loopback del clon de ensayo.')
credentials = json.loads(args.credentials_file.read_text())
fixtures = json.loads(args.fixtures_manifest.read_text())['fixtures']
assert len(fixtures) == 2 and {f['role'] for f in fixtures} == {'evaluador', 'evaluador2'}
results = []
args.output.parent.mkdir(parents=True, exist_ok=True)


def check(role, case, action):
    try:
        detail = action()
        results.append(dict(role=role, case=case, status='PASS', detail=detail))
        print('PASS', role, case, flush=True)
    except Exception as error:
        # No publicar los snapshots DOM de Playwright, que pueden contener PII de prueba.
        results.append(dict(role=role, case=case, status='FAIL', error=type(error).__name__))
        print('FAIL', role, case, type(error).__name__, flush=True)


def fullname(fixture):
    person = fixture['persona']
    return ' '.join(str(person.get(k) or '').strip() for k in ('nombres', 'apellidoPaterno', 'apellidoMaterno')).strip()


def sensitive(fixture):
    person = fixture['persona']
    values = [fullname(fixture), fixture['descripcion']]
    values.extend(str(person[k]) for k in ('numeroDocumento', 'correo', 'telefono', 'fechaNacimiento', 'numeroCarnetConadis') if person.get(k))
    return values


with sync_playwright() as pw:
    browser = pw.chromium.launch(executable_path='/usr/bin/chromium', headless=True, args=['--no-sandbox'])
    for index, own in enumerate(fixtures):
        other = fixtures[1-index]
        role = own['role']
        context = browser.new_context(viewport=dict(width=1440, height=1100))
        context.route('https://fonts.googleapis.com/**', lambda route: route.abort())
        context.route('https://fonts.gstatic.com/**', lambda route: route.abort())
        page = context.new_page()
        try:
            page.goto(args.url+'/', wait_until='networkidle')
            page.locator('#correo').fill(credentials[role]['correo'])
            page.locator('#clave').fill(credentials[role]['clave'])
            page.get_by_role('button', name='Ingresar', exact=True).click()
            page.wait_for_url(args.url+'/evaluador')
            page.goto(args.url+'/evaluador/observaciones', wait_until='networkidle')
            expect(page.locator('#alcance-observaciones')).to_be_visible()
            expect(page.locator('[data-privacy-scope=assigned-detail]')).to_be_visible()

            def search(text):
                field = page.locator('input[type=search]').last
                field.fill(text)
                field.press('Tab')

            def own_detail():
                search(own['codigo'])
                row = page.locator('tbody tr').filter(has_text=own['codigo'])
                expect(row).to_have_count(1)
                expect(row).to_contain_text(own['descripcion'])
                expect(row).to_contain_text(fullname(own))
                return dict(assigned_description=True, assigned_name=True)
            check(role, 'Mis conserva detalles autorizados', own_detail)

            def missing_other():
                search(other['codigo'])
                expect(page.locator('tbody tr').filter(has_text=other['codigo'])).to_have_count(0)
                assert all(value not in page.locator('main').inner_text() for value in sensitive(other))
                return dict(foreign_rows=0, foreign_pii_absent=True)
            check(role, 'Mis excluye expediente ajeno', missing_other)

            page.locator('#alcance-observaciones').select_option('todos')
            expect(page.locator('[data-privacy-scope=global-summary]')).to_be_visible()

            def global_metadata():
                for fixture in (own, other):
                    search(fixture['codigo'])
                    row = page.locator('tbody tr').filter(has_text=fixture['codigo'])
                    expect(row).to_have_count(1)
                    expect(row).to_contain_text('Otro')
                    expect(row).to_contain_text('Por subsanar')
                return dict(global_fixture_rows=2, code_type_state_preserved=True)
            check(role, 'Todos conserva los dos expedientes como resumen', global_metadata)

            def global_minimal():
                search('')
                text = page.locator('[data-privacy-scope=global-summary]').inner_text()
                assert all(value not in text for fixture in (own, other) for value in sensitive(fixture))
                expect(page.locator('#alcance-observaciones option:checked')).to_have_text('Todos los expedientes')
                return dict(names_contacts_birth_disability_text_absent=True, selected_scope_preserved=True)
            check(role, 'Todos no presenta PII ni texto privado', global_minimal)

            def no_foreign_action():
                search(other['codigo'])
                row = page.locator('tbody tr').filter(has_text=other['codigo'])
                expect(row.get_by_role('link', name='Abrir revisión', exact=True)).to_have_count(0)
                expect(row.get_by_role('button')).to_have_count(0)
                return dict(foreign_detail_or_write_actions=0)
            check(role, 'Resumen ajeno no ofrece acciones privadas', no_foreign_action)

            def own_review():
                page.goto(args.url+'/evaluador/postulaciones/'+str(own['postulacionId']), wait_until='networkidle')
                expect(page.locator('main')).to_contain_text(fullname(own))
                expect(page.locator('main')).to_contain_text(own['persona']['numeroDocumento'])
                return dict(assigned_review=True)
            check(role, 'Revisión asignada sigue disponible', own_review)

            def foreign_review():
                page.goto(args.url+'/evaluador/postulaciones/'+str(other['postulacionId']), wait_until='networkidle')
                expect(page.locator('main')).to_contain_text('No tienes acceso a esta postulación')
                assert all(value not in page.locator('main').inner_text() for value in sensitive(other))
                return dict(foreign_route_without_private_data=True)
            check(role, 'URL directa ajena no revela detalles', foreign_review)

            page.goto(args.url+'/evaluador/vouchers', wait_until='networkidle')
            page.get_by_role('button', name=re.compile('^Aprobado')).click()

            def own_payment():
                search(own['codigo'])
                expect(page.locator('tbody tr').filter(has_text=own['codigo'])).to_have_count(1)
                return dict(assigned_voucher_visible=True)
            check(role, 'Voucher asignado sigue visible', own_payment)

            def no_foreign_payment():
                search(other['codigo'])
                expect(page.locator('tbody tr').filter(has_text=other['codigo'])).to_have_count(0)
                assert all(value not in page.locator('main').inner_text() for value in sensitive(other))
                return dict(foreign_voucher_rows=0, foreign_pii_absent=True)
            check(role, 'Vouchers ajenos no se presentan', no_foreign_payment)
            assert not page.locator('#blazor-error-ui').is_visible()
        except Exception as error:
            results.append(dict(role=role, case='Preparación o navegación', status='FAIL', error=type(error).__name__))
        finally:
            try:
                page.screenshot(path=str(args.output.parent/('privacidad-ui-'+role+'.png')), full_page=True)
            except Exception:
                pass
            context.close()
    browser.close()

summary = dict(cases=len(results), passed=sum(row['status']=='PASS' for row in results), failed=sum(row['status']=='FAIL' for row in results))
args.output.write_text(json.dumps(dict(environment='Codex Cloud; portal del clon; no WSL/AWS/base original', identities=dict(evaluadores=2), summary=summary, cases=results), ensure_ascii=False, indent=2)+'\n')
print(json.dumps(summary), flush=True)
raise SystemExit(1 if summary['failed'] else 0)
