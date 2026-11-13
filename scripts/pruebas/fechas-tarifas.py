#!/usr/bin/env python3
"""H02/H03: fixtures nuevas, REST real y contraste SQL opcional de admitu_db local."""
import argparse
import json
import secrets
import subprocess
import time
from datetime import datetime, timedelta
from decimal import Decimal
from pathlib import Path
from zoneinfo import ZoneInfo
from regresion import API


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--base-url', default='http://localhost:8080/admitu_services/webresources')
    p.add_argument('--credentials-file', type=Path, required=True)
    p.add_argument('--mysql-defaults', type=Path, help='Archivo de cliente local privado; solo SELECT a admitu_db')
    p.add_argument('--output', type=Path, required=True)
    args = p.parse_args()
    api = API(args.base_url)
    cred = json.loads(args.credentials_file.read_text())
    admin = api.login(cred['admin'])
    day = datetime.now(ZoneInfo('America/Lima')).date()
    tag = 'FT' + secrets.token_hex(4).upper()
    results = []
    fixtures = []

    def get(path, token=admin):
        return api.json(path, token=token)

    def insert(entity, payload):
        identity = api.json(f'{entity}RS/insertar{entity}', 'POST', payload, admin)
        assert isinstance(identity, int) and identity > 0
        return identity

    def sql(query):
        command = ['mysql', f'--defaults-extra-file={args.mysql_defaults.resolve()}', '--batch', '--skip-column-names', 'admitu_db', '-e', query]
        return subprocess.run(command, check=True, text=True, capture_output=True).stdout.strip()

    # No se modifican fixtures históricas. Cada ejecución conserva su prefijo.
    modality = insert('Modalidad', dict(codigoModalidad=tag, nombre=tag, descripcion='Ensayo fechas/tarifas', requiereColegio=False, requiereUniversidad=False, activo=True))
    career = next(x['id'] for x in get('CarreraRS/listarTodasCarreras') if x['activo'])
    site = next(x['id'] for x in get('SedeRS/listarTodasSedes') if x['activo'])
    account = dict(correo=tag.lower()+'@example.test', clave=secrets.token_urlsafe(24))
    person = api.json('AutenticacionRS/registrar', 'POST', dict(clave=account['clave'], postulante=dict(nombres='Ensayo', apellidoPaterno='Fechas', apellidoMaterno=tag, correo=account['correo'], tipoDocumento='DNI', numeroDocumento=str(20000000+secrets.randbelow(60000000)), fechaNacimiento='2000-01-01', telefono='999000001')), '')
    user = api.login(account)
    for index, (offset, cost) in enumerate(((1, Decimal('111.23')), (0, Decimal('222.34')), (-1, Decimal('333.45'))), 1):
        label = {1:'antes del cierre', 0:'día exacto del cierre', -1:'después del cierre'}[offset]
        call = dict(codigoConvocatoria=tag+str(index), nombre=tag+str(index), periodo='2026-2', fechaInicio=str(day-timedelta(days=20)), fechaFin=str(day+timedelta(days=offset)), estado='PUBLICADA', descripcion='Fixture nueva H02/H03', activo=True)
        conv = insert('Convocatoria', call)
        bridge = insert('ConvocatoriaModalidad', dict(convocatoria={'id':conv}, modalidad={'id':modality}, costoInscripcion=float(cost), observacion='Fixture nueva H03'))
        offer = insert('OfertaCarrera', dict(convocatoria={'id':conv}, carrera={'id':career}, cantidadVacantes=10))
        fixture = dict(convocatoria=conv, convocatoria_modalidad=bridge, modalidad=modality, tarifa=str(cost), cierre=call['fechaFin'])
        fixtures.append(fixture)
        start = time.perf_counter()
        before = get('PostulacionRS/listarTodasPostulaciones', user)
        response, _, body = api.call('PostulacionRS/enviarPostulacion', 'POST', dict(postulacion=dict(postulante={'id':person}, convocatoria={'id':conv}, modalidadElegida={'id':bridge}, carreraElegida={'id':offer}, estadoActual={'id':2}, codigoInscripcion=tag+str(index), sedeId=site), sedeId=site, documentos=[]), user)
        after = get('PostulacionRS/listarTodasPostulaciones', user)
        if offset >= 0:
            assert 200 <= response < 300, f'{label}: HTTP {response}'
            submission = json.loads(body)
            payment = next(x for x in get('PagoRS/listarTodosPagos', user) if x['postulacion']['id'] == submission)
            assert Decimal(str(payment['monto'])) == cost, 'Pago no coincide con tarifa convocatoria_modalidad'
            assert len(after) == len(before)+1
            fixture['postulacion'] = submission
            fixture['pago'] = payment['id']
            if args.mysql_defaults:
                actual = sql(f'SELECT CAST(cm.costo_inscripcion AS CHAR), CAST(p.monto AS CHAR) FROM pago p JOIN postulacion s ON s.id=p.postulacion_id JOIN convocatoria_modalidad cm ON cm.id=s.modalidad_elegida_id WHERE p.id={payment["id"]};')
                expected = '\t'.join((str(cost), str(cost)))
                assert actual == expected, 'Contraste SQL distinto a REST'
        else:
            assert response in (400, 409, 422), f'{label}: HTTP {response}'
            assert after == before, 'Un rechazo cambió postulaciones'
            assert all(x['postulacion']['id'] in {a['id'] for a in before} for x in get('PagoRS/listarTodosPagos', user)), 'Rechazo creó pago'
            if args.mysql_defaults:
                assert sql(f'SELECT COUNT(*) FROM postulacion WHERE convocatoria_id={conv} AND postulante_id={person};') == '0'
        results.append(dict(caso=label, estado='PASS', http=response, tarifa=str(cost), contraste_sql=bool(args.mysql_defaults), seconds=round(time.perf_counter()-start,3)))
    assert fixtures[0]['modalidad'] == fixtures[1]['modalidad'] and fixtures[0]['tarifa'] != fixtures[1]['tarifa']
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(dict(entorno='Cloud; REST y SQL local de ensayo si contraste_sql=true; sin WSL ni AWS', dia_lima=str(day), prefijo_fixtures=tag, fixtures=fixtures, resultados=results), ensure_ascii=False, indent=2)+'\n')
    print(f'{len(results)} PASS; {args.output}')

if __name__ == '__main__':
    main()
