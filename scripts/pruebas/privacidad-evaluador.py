#!/usr/bin/env python3
"""Privacidad REST en un clon de ensayo: cinco identidades, JSON y alcance SQL.

No ejecuta mutaciones sin --apply y --expected-server. El operador debe verificar
que tanto el API como el cliente MySQL apuntan al clon, nunca a la base original.
Las evidencias contienen campos/contadores/huellas, no valores personales.
"""
import argparse
import base64
import copy
import json
import secrets
import subprocess
import sys
from pathlib import Path
from urllib.parse import urlsplit

import importlib.util

spec = importlib.util.spec_from_file_location('auditoria_seguridad', Path(__file__).with_name('auditoria-seguridad.py'))
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)
from regresion import PNG, pdf

EXPEDIENTE_KEYS = {
    'id', 'codigoInscripcion', 'convocatoriaId', 'modalidadElegidaId',
    'carreraElegidaId', 'estadoActualId', 'evaluadorId', 'fechaRegistro',
    'fechaEnvio', 'fechaFinalizacion', 'observaciones',
}
OBSERVACION_KEYS = {
    'id', 'postulacionId', 'requisitoAplicableId', 'evaluadorId',
    'tipoObservacion', 'estadoObservacion', 'fechaObservacion', 'fechaSubsanacion',
}
PII_KEYS = {'numeroDocumento', 'correo', 'telefono', 'fechaNacimiento',
            'tieneDiscapacidad', 'numeroCarnetConadis'}
RESOURCES = {
    'Postulante': ('PostulanteRS/listarTodosPostulantes', 'PostulanteRS/buscarPostulantePorId/',
                   'SELECT DISTINCT p.postulante_id FROM postulacion p WHERE p.evaluador_id={ev}'),
    'AntecedenteAcademico': ('AntecedenteAcademicoRS/listarTodosAntecedentesAcademicos', 'AntecedenteAcademicoRS/buscarAntecedenteAcademicoPorId/',
                            'SELECT DISTINCT a.id FROM antecedente_academico a JOIN postulacion p ON p.postulante_id=a.postulante_id WHERE p.evaluador_id={ev}'),
    'Postulacion': ('PostulacionRS/listarTodasPostulaciones', 'PostulacionRS/buscarPostulacionPorId/',
                    'SELECT id FROM postulacion WHERE evaluador_id={ev}'),
    'Pago': ('PagoRS/listarTodosPagos', 'PagoRS/buscarPagoPorId/',
             'SELECT g.id FROM pago g JOIN postulacion p ON p.id=g.postulacion_id WHERE p.evaluador_id={ev}'),
    'DocumentoPostulacion': ('DocumentoPostulacionRS/listarTodosDocumentosPostulacion', 'DocumentoPostulacionRS/buscarDocumentoPostulacionPorId/',
                             'SELECT d.id FROM documento_postulacion d JOIN postulacion p ON p.id=d.postulacion_id WHERE p.evaluador_id={ev}'),
    'DocumentoObservacion': ('DocumentoObservacionRS/listarTodasDocumentoObservaciones', 'DocumentoObservacionRS/buscarDocumentoObservacionPorId/',
                             'SELECT o.id FROM documento_observacion o JOIN documento_postulacion d ON d.id=o.documento_id JOIN postulacion p ON p.id=d.postulacion_id WHERE p.evaluador_id={ev}'),
    'PostulacionHistorial': ('PostulacionHistorialRS/listarTodosPostulacionHistorial', 'PostulacionHistorialRS/buscarPostulacionHistorialPorId/',
                            'SELECT h.id FROM postulacion_historial h JOIN postulacion p ON p.id=h.postulacion_id WHERE p.evaluador_id={ev}'),
    'CarnePostulante': ('CarnePostulanteRS/listarTodosCarnesPostulante', 'CarnePostulanteRS/buscarCarnePostulantePorId/',
                       'SELECT c.id FROM carne_postulante c JOIN postulacion p ON p.id=c.postulacion_id WHERE p.evaluador_id={ev}'),
}


class Privacidad(audit.Auditoria):
    def __init__(self, args):
        # Guardar el destino antes de cualquier registro/login por HTTP.
        host = urlsplit(args.base_url).hostname
        if host not in ('127.0.0.1', 'localhost'):
            raise RuntimeError('El API de esta prueba debe ser loopback del clon verificado')
        probe = subprocess.run(['mysql', '--defaults-extra-file='+str(args.mysql_defaults_file.resolve()),
                                '--protocol=TCP', '--host=127.0.0.1', '--batch', '--raw',
                                '--skip-column-names', 'admitu_db'],
                               input='SELECT @@hostname,DATABASE();', text=True, capture_output=True)
        if probe.returncode or probe.stdout.strip().split('\t') != [args.expected_server, 'admitu_db']:
            raise RuntimeError('Destino MySQL no coincide con el clon autorizado')
        super().__init__(args)
        self.tag = 'PRIV'+secrets.token_hex(4).upper()

    def prepare(self):
        super().prepare()
        person2 = self.json('PostulanteRS/buscarPostulantePorId/'+str(self.person2))
        person2.update(telefono='977000002', tieneDiscapacidad=True,
                       numeroCarnetConadis='PRIV'+secrets.token_hex(4))
        self.json('PostulanteRS/modificarPostulante', 'PUT', person2)
        self.people = [self.json('PostulanteRS/buscarPostulantePorId/'+str(i))
                       for i in (self.person, self.person2)]
        post2 = self.json('PostulacionRS/buscarPostulacionPorId/'+str(self.post2id))
        pay2 = self.json('PagoRS/listarTodosPagos', token=self.user2)[0]
        doc2 = self.json('DocumentoPostulacionRS/listarTodosDocumentosPostulacion', token=self.user2)[0]
        self.fixtures = []
        for index, (person, token, evtoken, evid, post, pay, doc) in enumerate([
            (self.person, self.user, self.tokens['evaluador'], self.ev1, self.post, self.pago, self.doc),
            (self.person2, self.user2, self.tokens['evaluador2'], self.ev2, post2, pay2, doc2),
        ], 1):
            date = str(self.fixture.today)
            cert = self.json('ArchivoDigitalRS/guardarCertificado', 'POST',
                             dict(postulanteId=person, tipo='CERTIFICADO', referenciaId=0,
                                  nombre='certificado.pdf', mime='application/pdf',
                                  contenidoBase64=base64.b64encode(pdf()).decode(), fechaRegistro=date,
                                  institucion='Colegio '+self.tag+str(index), anioInicio=2017, anioFin=2021), token)
            self.json('ArchivoDigitalRS/guardarVoucher', 'POST',
                      dict(postulanteId=person, tipo='VOUCHER', referenciaId=pay['id'],
                           nombre='voucher.png', mime='image/png', contenidoBase64=base64.b64encode(PNG).decode(),
                           fechaRegistro=date, codigoOperacion=self.tag+str(index), monto=100), token)
            description = 'Descripcion privada '+self.tag+str(index)
            oid = self.json('DocumentoObservacionRS/insertarDocumentoObservacion', 'POST',
                            dict(documento={'id':doc['id']}, evaluador={'id':evid}, tipoObservacion='OTRO',
                                 descripcion=description, fechaObservacion=date, estadoObservacion='PENDIENTE'), evtoken)
            self.json('PagoRS/modificarPago', 'PUT', dict(pay, estadoPago='APROBADO'), evtoken)
            self.json('DocumentoPostulacionRS/modificarDocumentoPostulacion', 'PUT',
                      dict(doc, estadoDocumento='APROBADO'), evtoken)
            self.json('PostulacionRS/modificarPostulacion', 'PUT', dict(post, estadoActual={'id':5}), evtoken)
            cid = self.json('CarnePostulanteRS/insertarCarnePostulante', 'POST',
                            dict(postulacion={'id':post['id']}, sede={'id':self.fixture.sede},
                                 codigoCarne='C-'+self.tag+str(index), fechaGeneracion=date,
                                 fechaInicioVigencia=date, fechaFinVigencia=date, aulaExamen='PRIV'))
            history = self.json('PostulacionHistorialRS/listarTodosPostulacionHistorial', token=token)
            hid = next(h['id'] for h in history if h['postulacion']['id']==post['id'])
            self.fixtures.append(dict(person=person, token=token, evtoken=evtoken, evid=evid,
                                      post=post['id'], code=post['codigoInscripcion'], description=description,
                                      ids={'Postulante':person, 'AntecedenteAcademico':cert['referenciaId'],
                                           'Postulacion':post['id'], 'Pago':pay['id'],
                                           'DocumentoPostulacion':doc['id'], 'DocumentoObservacion':oid,
                                           'PostulacionHistorial':hid, 'CarnePostulante':cid}))
        self.pii_values = [{str(p[k]) for k in PII_KEYS if p.get(k) is not None
                            and not isinstance(p[k], bool)} for p in self.people]
        if self.a.fixtures_manifest:
            manifest = dict(fixtures=[dict(role='evaluador' if i==0 else 'evaluador2',
                            evaluadorId=f['evid'], personaId=f['person'], postulacionId=f['post'],
                            codigo=f['code'], ids=f['ids'], descripcion=f['description'], persona=self.people[i])
                            for i,f in enumerate(self.fixtures)])
            self.a.fixtures_manifest.parent.mkdir(parents=True,exist_ok=True)
            self.a.fixtures_manifest.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
            self.a.fixtures_manifest.chmod(0o600)

    def full_record(self, path, identifier, token, forbidden_person=None, profile=None):
        status, _, body = self.api.call(path+str(identifier), token=token)
        assert status==200, 'Consulta autorizada debe responder HTTP200'
        data=json.loads(body);assert data['id']==identifier, 'El ID retornado debe ser el solicitado'
        if path.startswith('DocumentoObservacionRS/') and token!=self.tokens['admin']:
            assert isinstance(data.get('evaluador'),dict) and set(data['evaluador'])=={'id'}, 'Observacion no admin expone datos personales del evaluador autor'
        if forbidden_person is not None:
            assert not any(value in body.decode() for value in self.pii_values[forbidden_person]), 'Datos personales de otro titular en respuesta autorizada'
        if profile is not None:
            assert PII_KEYS <= set(data), 'Perfil autorizado incompleto: faltan campos personales'
            assert all(data.get(key)==profile.get(key) for key in PII_KEYS), 'Perfil autorizado no coincide con su titular'
        return dict(http=status, fields=sorted(data), private_values_not_published=True)

    def lists_and_ids(self):
        for entity, (listing, individual, query) in RESOURCES.items():
            def admin_list(listing=listing, entity=entity):
                rows=self.json(listing);ids={r['id'] for r in rows}
                assert all(f['ids'][entity] in ids for f in self.fixtures), 'Admin debe ver ambos fixtures'
                return dict(http=200, fixtures=2)
            self.check('Listado admin completo '+entity, admin_list)
            self.expect('Listado anonimo '+entity, listing, 401)
            for i, fixture in enumerate(self.fixtures):
                other=self.fixtures[1-i];identifier=fixture['ids'][entity]
                def evaluator_list(listing=listing, query=query, fixture=fixture, other=other, entity=entity, i=i):
                    rows=self.json(listing, token=fixture['evtoken'])
                    expected={int(x) for x in self.sql(query.format(ev=fixture['evid'])).splitlines()}
                    assert {r['id'] for r in rows}==expected, 'Listado evaluador contiene IDs fuera de su asignacion'
                    assert fixture['ids'][entity] in expected and other['ids'][entity] not in expected, 'Fixtures no separan asignaciones'
                    serialized=json.dumps(rows, ensure_ascii=False)
                    assert not any(value in serialized for value in self.pii_values[1-i]), 'Listado evaluador filtra PII de otro titular'
                    return dict(http=200, rows=len(rows), sql_scope_exact=True, foreign_private_values_absent=True)
                self.check('Listado asignado evaluador'+str(i+1)+' '+entity, evaluator_list)
                self.check('ID asignado evaluador'+str(i+1)+' '+entity,
                           lambda individual=individual, identifier=identifier, fixture=fixture, i=i, entity=entity:
                           self.full_record(individual, identifier, fixture['evtoken'], 1-i,
                                            self.people[i] if entity=='Postulante' else None))
                self.check('ID propio postulante'+str(i+1)+' '+entity,
                           lambda individual=individual, identifier=identifier, fixture=fixture, i=i, entity=entity:
                           self.full_record(individual, identifier, fixture['token'], 1-i,
                                            self.people[i] if entity=='Postulante' else None))
                self.check('ID admin titular'+str(i+1)+' '+entity,
                           lambda individual=individual, identifier=identifier, i=i, entity=entity:
                           self.full_record(individual, identifier, self.tokens['admin'], None,
                                            self.people[i] if entity=='Postulante' else None))
                self.expect('ID ajeno evaluador'+str(2-i)+' '+entity, individual+str(identifier), 403, token=other['evtoken'])
                self.expect('ID ajeno postulante'+str(2-i)+' '+entity, individual+str(identifier), 403, token=other['token'])
                self.expect('ID anonimo titular'+str(i+1)+' '+entity, individual+str(identifier), 401)

    def global_summary(self, token):
        status, _, body = self.api.call('PostulacionRS/listarResumenesGlobales', token=token)
        assert status==200, 'Resumen global autorizado debe responder200'
        rows=json.loads(body);assert isinstance(rows,list)
        assert all(any(row['id']==f['post'] for row in rows) for f in self.fixtures), 'Todos debe conservar expedientes de ambos evaluadores'
        required={'id','codigoInscripcion','convocatoriaId','modalidadElegidaId','carreraElegidaId','estadoActualId','observaciones'}
        found_observations=set()
        for row in rows:
            assert required<=set(row)<=EXPEDIENTE_KEYS, 'Contrato global expone campos no autorizados o pierde metadatos'
            for key,value in row.items():
                if key=='observaciones':
                    assert isinstance(value,list)
                    for obs in value:
                        assert {'id','postulacionId','requisitoAplicableId','evaluadorId','tipoObservacion','estadoObservacion'}<=set(obs)<=OBSERVACION_KEYS, 'Contrato observacion global contiene campos no autorizados'
                        assert obs['postulacionId']==row['id']
                        assert all(not isinstance(v,(dict,list)) for v in obs.values()), 'Resumen no debe incluir entidades anidadas'
                        found_observations.add(obs['id'])
                else:
                    assert not isinstance(value,(dict,list)), 'Resumen no debe incluir entidades anidadas'
        assert all(f['ids']['DocumentoObservacion'] in found_observations for f in self.fixtures), 'Todos debe conservar metadatos de observacion de ambos evaluadores'
        forbidden=set().union(*self.pii_values)|{f['description'] for f in self.fixtures}
        text=body.decode()
        assert not any(value in text for value in forbidden), 'PII o texto privado presente en resumen global'
        return dict(http=200, expedientes=len(rows), both_assignment_summaries=True,
                    expediente_keys_allowlist=sorted(EXPEDIENTE_KEYS), observacion_keys_allowlist=sorted(OBSERVACION_KEYS),
                    private_values_absent=True, no_nested_domain_entities=True)

    def global_and_evaluators(self):
        for role in ('admin','evaluador','evaluador2'):
            self.check('Resumen global DTO '+role, lambda role=role:self.global_summary(self.tokens[role]))
        self.expect('Resumen global anonimo', 'PostulacionRS/listarResumenesGlobales', 401)
        for i,fixture in enumerate(self.fixtures):
            self.expect('Resumen global postulante'+str(i+1), 'PostulacionRS/listarResumenesGlobales', 403, token=fixture['token'])
            def own_evaluator(fixture=fixture):
                rows=self.json('EvaluadorRS/listarTodosEvaluadores', token=fixture['evtoken'])
                assert [r['id'] for r in rows]==[fixture['evid']], 'No se admite directorio personal de otros evaluadores'
                row=self.json('EvaluadorRS/buscarEvaluadorPorId/'+str(fixture['evid']), token=fixture['evtoken'])
                assert row['id']==fixture['evid']
                return dict(http=200, own_evaluator_only=True)
            self.check('Evaluador propio '+str(i+1),own_evaluator)
            self.expect('Evaluador no consulta otro evaluador '+str(i+1),
                        'EvaluadorRS/buscarEvaluadorPorId/'+str(self.fixtures[1-i]['evid']),403,token=fixture['evtoken'])

    def pdf_scope(self, index, expected_indices, admin=False):
        fixture=self.fixtures[index]
        path='ReporteRS/generarObservados?convocatoriaId='+str(self.fixture.conv['id'])
        status,headers,data=self.api.call(path,token=self.tokens['admin'] if admin else fixture['evtoken'])
        assert status==200 and data.startswith(b'%PDF-'), 'Reporte asignado debe ser PDF real'
        folder=self.a.output.parent/'privacidad-pdf';folder.mkdir(exist_ok=True)
        file=folder/(('admin' if admin else 'evaluador'+str(index+1))+'.pdf');file.write_bytes(data)
        text=subprocess.check_output(['pdftotext',str(file),'-'],text=True)
        for i,f in enumerate(self.fixtures):
            assert (f['description'] in text)==(i in expected_indices), 'PDF contiene texto privado fuera de asignacion o pierde el propio'
        return dict(http=200, bytes=len(data), role_scope='admin_global' if admin else 'asignacion_actual', private_text_not_published=True)

    def same_owner_payment_scope(self):
        # Otro expediente del mismo titular, aún sin asignar, no concede acceso a su pago.
        call=dict(self.fixture.conv, id=0, codigoConvocatoria=self.tag+'C', nombre='Convocatoria '+self.tag)
        cid=self.json('ConvocatoriaRS/insertarConvocatoria','POST',call)
        cmid=self.json('ConvocatoriaModalidadRS/insertarConvocatoriaModalidad','POST',
                       dict(convocatoria={'id':cid},modalidad={'id':self.fixture.ids['Modalidad']},costoInscripcion=200))
        offerid=self.json('OfertaCarreraRS/insertarOfertaCarrera','POST',
                          dict(convocatoria={'id':cid},carrera={'id':self.fixture.ids['Carrera']},cantidadVacantes=5))
        payload=copy.deepcopy(self.payload)
        payload['postulacion'].update(convocatoria={'id':cid},modalidadElegida={'id':cmid},carreraElegida={'id':offerid},codigoInscripcion=self.tag+'OTRA')
        payload['documentos']=[]
        pid=self.json('PostulacionRS/enviarPostulacion','POST',payload,self.user)
        pay=next(p for p in self.json('PagoRS/listarTodosPagos',token=self.user) if p['postulacion']['id']==pid)
        self.expect('Mismo titular no habilita pago de otro expediente sin asignar',
                    'PagoRS/buscarPagoPorId/'+str(pay['id']),403,token=self.tokens['evaluador'])
        self.expect('Mismo titular no habilita expediente sin asignar',
                    'PostulacionRS/buscarPostulacionPorId/'+str(pid),403,token=self.tokens['evaluador'])
        def lists():
            assert pay['id'] not in {r['id'] for r in self.json('PagoRS/listarTodosPagos',token=self.tokens['evaluador'])}
            assert pid not in {r['id'] for r in self.json('PostulacionRS/listarTodasPostulaciones',token=self.tokens['evaluador'])}
            assert self.json('PostulanteRS/buscarPostulantePorId/'+str(self.person),token=self.tokens['evaluador'])['id']==self.person
            return dict(http=200, same_owner_does_not_expand_expediente_scope=True, assigned_profile_remains_accessible=True)
        self.check('Listas no amplian alcance por compartir titular',lists)

    def revocation(self):
        first,second=self.fixtures
        self.json(f'PostulacionRS/asignarEvaluador/{first["post"]}/{second["evid"]}','PUT')
        for entity,(_,individual,_) in RESOURCES.items():
            self.expect('Revocacion ID '+entity,individual+str(first['ids'][entity]),403,token=first['evtoken'])
            self.check('Nuevo asignado ID '+entity,
                       lambda individual=individual, entity=entity:self.full_record(individual,first['ids'][entity],second['evtoken']))
        self.check('Resumen global persiste sin asignacion',lambda:self.global_summary(first['evtoken']))
        self.check('PDF revocado no contiene ambos titulares',lambda:self.pdf_scope(0,set()))
        self.check('PDF nuevo asignado contiene ambos titulares',lambda:self.pdf_scope(1,{0,1}))
        self.json(f'PostulacionRS/asignarEvaluador/{first["post"]}/{first["evid"]}','PUT')

    def run(self):
        try:
            self.prepare();self.lists_and_ids();self.global_and_evaluators();self.same_owner_payment_scope()
            self.check('PDF evaluador1 sin texto titular2',lambda:self.pdf_scope(0,{0}))
            self.check('PDF evaluador2 sin texto titular1',lambda:self.pdf_scope(1,{1}))
            self.check('PDF admin conserva ambos titulares',lambda:self.pdf_scope(0,{0,1},admin=True))
            self.expect('PDF observado anonimo denegado','ReporteRS/generarObservados',401)
            for i,fixture in enumerate(self.fixtures):
                self.expect('PDF observado postulante'+str(i+1)+' denegado','ReporteRS/generarObservados',403,token=fixture['token'])
            self.revocation()
        except Exception as exc:
            self.results.append(dict(case='Preparacion y ejecucion completa',status='FAIL',detail=type(exc).__name__))
            print('FAIL Preparacion y ejecucion completa '+type(exc).__name__,flush=True)
        finally:
            self.a.output.parent.mkdir(parents=True,exist_ok=True)
            passed=sum(r['status']=='PASS' for r in self.results)
            negative=sum(isinstance(r.get('detail'),dict) and r['detail'].get('unchanged') is True for r in self.results)
            result=dict(environment='Codex Cloud; clon MySQL propio; no WSL/AWS/base original',
                        identities=dict(admin=1,evaluadores=2,postulantes=2),
                        summary=dict(cases=len(self.results),passed=passed,failed=len(self.results)-passed,negative_http_sql=negative),
                        snapshot_tables=list(audit.TABLES),cases=self.results)
            self.a.output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
            print(str(passed)+'/'+str(len(self.results))+' PASS; '+str(negative)+' negativos HTTP/SQL.',flush=True)
        return 1 if any(r['status']=='FAIL' for r in self.results) else 0


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply',action='store_true')
    parser.add_argument('--base-url',default='http://127.0.0.1:8080/admitu_services/webresources/')
    parser.add_argument('--credentials-file',type=Path,required=True)
    parser.add_argument('--mysql-defaults-file',type=Path,required=True)
    parser.add_argument('--expected-server')
    parser.add_argument('--fixtures-manifest',type=Path,help='Manifest privado de IDs y datos sintéticos; nunca versionar')
    parser.add_argument('--output',type=Path,default=Path('work/evidencias/privacidad-evaluador.json'))
    args=parser.parse_args()
    if not args.apply:
        print('DRY-RUN: requiere --apply y destino clon verificado; no se conecto a la base.');return 0
    if not args.expected_server:parser.error('--apply requiere --expected-server del clon')
    return Privacidad(args).run()


if __name__=='__main__':sys.exit(main())
