package pe.edu.pucp.admitu.pruebas;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import pe.edu.pucp.admitu.archivo.*;
import pe.edu.pucp.admitu.archivo.bo.*;
import pe.edu.pucp.admitu.config.*;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.pago.*;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;

/** Integración opt-in contra admitu_db de ensayo. Crea y limpia solo fixtures propios. */
public final class PruebasReglasLab12 {
    private static int persona;
    private static int convocatoria,modalidad,oferta,sede;
    private static final byte[] PNG=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=");
    private static final byte[] PDF="%PDF-1.4\n% fixture integración\n%%EOF".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static int checks;
    private static void check(boolean pass,String label){if(!pass)throw new AssertionError(label);System.out.println("PASS "+(++checks)+" "+label);}
    private static int scalar(String sql)throws SQLException {try(Connection c=DBManager.getInstance().getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private static void setup()throws Exception {
        try(Connection c=DBManager.getInstance().getConnection();Statement s=c.createStatement()){
            try(ResultSet r=s.executeQuery("SELECT DATABASE()")){r.next();if(!"admitu_db".equals(r.getString(1)))throw new IllegalStateException("Solo admitu_db");}
            try(ResultSet r=s.executeQuery("SELECT c.id,cm.id,o.id FROM convocatoria c JOIN convocatoria_modalidad cm ON cm.convocatoria_id=c.id JOIN oferta_carrera o ON o.convocatoria_id=c.id WHERE c.activo=1 AND c.estado='PUBLICADA' AND c.fecha_inicio<=CURDATE() AND c.fecha_fin>=CURDATE() LIMIT 1")){if(!r.next())throw new IllegalStateException("Se requiere convocatoria abierta de ensayo");convocatoria=r.getInt(1);modalidad=r.getInt(2);oferta=r.getInt(3);}
            try(ResultSet r=s.executeQuery("SELECT id FROM sede WHERE activo=1 LIMIT 1")){r.next();sede=r.getInt(1);}
            String tag=UUID.randomUUID().toString().replace("-","");
            try(PreparedStatement p=c.prepareStatement("INSERT INTO persona(nombres,apellido_paterno,correo,tipo_documento,numero_documento) VALUES('Prueba','Lab12',?,'CE',?)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,"lab12-"+tag+"@example.test");p.setString(2,tag.substring(0,16));p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();persona=k.getInt(1);}}
            s.executeUpdate("INSERT INTO postulante(persona_id,fecha_nacimiento) VALUES("+persona+",'2000-01-01')");
        }
    }
    private static EnvioPostulacion envio(boolean docs)throws Exception {
        Postulacion p=new Postulacion();Postulante per=new Postulante();per.setId(persona);p.setPostulante(per);
        Convocatoria c=new Convocatoria();c.setId(convocatoria);p.setConvocatoria(c);
        ConvocatoriaModalidad m=new ConvocatoriaModalidad();m.setId(modalidad);p.setModalidadElegida(m);
        OfertaCarrera o=new OfertaCarrera();o.setId(oferta);p.setCarreraElegida(o);
        EstadoPostulacion e=new EstadoPostulacion();e.setId(6);p.setEstadoActual(e); // El servidor fuerza estado inicial.
        EnvioPostulacion envio=new EnvioPostulacion();envio.setPostulacion(p);envio.setSedeId(sede);List<ArchivoDigital> list=new ArrayList<>();
        if(docs)try(Connection cn=DBManager.getInstance().getConnection();Statement s=cn.createStatement();ResultSet r=s.executeQuery("SELECT rm.id,r.tipo_archivo_requerido FROM requisito_convocatoria_modalidad rm JOIN requisito r ON r.id=rm.requisito_id WHERE rm.convocatoria_modalidad_id="+modalidad+" AND rm.obligatorio=1")){
            while(r.next()){boolean pdf=r.getString(2).equals("PDF");ArchivoDigital a=new ArchivoDigital();a.setRequisitoAplicableId(r.getInt(1));a.setNombre(pdf?"fixture.pdf":"fixture.png");a.setMime(pdf?"application/pdf":"image/png");a.setContenidoBase64(Base64.getEncoder().encodeToString(pdf?PDF:PNG));list.add(a);}
        }
        envio.setDocumentos(list);return envio;
    }
    private static ArchivoDigital personal(String tipo){ArchivoDigital a=new ArchivoDigital();a.setPostulanteId(persona);a.setTipo(tipo);a.setNombre("fixture.png");a.setMime("image/png");a.setContenidoBase64(Base64.getEncoder().encodeToString(PNG));return a;}
    private static void cleanup()throws SQLException {
        if(persona<=0)return;
        try(Connection c=DBManager.getInstance().getConnection();Statement s=c.createStatement()){
            c.setAutoCommit(false);
            s.executeUpdate("DELETE FROM archivo_digital WHERE postulante_id="+persona);
            for(String table:List.of("notificacion","documento_observacion","documento_postulacion","carne_postulante","pago","postulacion_historial")){
                String condition=table.equals("documento_observacion")?"documento_id IN(SELECT id FROM documento_postulacion WHERE postulacion_id IN(SELECT id FROM postulacion WHERE postulante_id="+persona+"))":"postulacion_id IN(SELECT id FROM postulacion WHERE postulante_id="+persona+")";
                s.executeUpdate("DELETE FROM "+table+" WHERE "+condition);
            }
            s.executeUpdate("DELETE FROM postulacion WHERE postulante_id="+persona);s.executeUpdate("DELETE FROM antecedente_academico WHERE postulante_id="+persona);s.executeUpdate("DELETE FROM postulante WHERE persona_id="+persona);s.executeUpdate("DELETE FROM persona WHERE id="+persona);c.commit();
        }
    }
    public static void main(String[] args)throws Exception {
        if(!"true".equals(System.getenv("ADMITU_PRUEBAS_ESCRITURA")))throw new IllegalStateException("Exige ADMITU_PRUEBAS_ESCRITURA=true y destino ensayo propio ya verificado");
        try{
            setup();
            boolean fallo=false;try{new PostulacionBOImpl().enviar(envio(false));}catch(RuntimeException expected){fallo=true;}
            check(fallo,"envío incompleto rechazado");
            check(scalar("SELECT COUNT(*) FROM postulacion WHERE postulante_id="+persona)==0,"rollback sin postulación parcial");
            check(scalar("SELECT COUNT(*) FROM pago p JOIN postulacion x ON x.id=p.postulacion_id WHERE x.postulante_id="+persona)==0,"rollback sin pago parcial");
            var barrier=new CyclicBarrier(2);ExecutorService pool=Executors.newFixedThreadPool(2);
            Callable<Integer> attempt=()->{var e=envio(true);barrier.await();try{return new PostulacionBOImpl().enviar(e);}catch(RuntimeException expected){return 0;}};
            var a=pool.submit(attempt);var b=pool.submit(attempt);int ia=a.get(30,TimeUnit.SECONDS),ib=b.get(30,TimeUnit.SECONDS);pool.shutdownNow();
            check((ia>0)^(ib>0),"dos envíos concurrentes producen un éxito");int post=Math.max(ia,ib);
            check(scalar("SELECT COUNT(*) FROM postulacion WHERE postulante_id="+persona)==1,"una postulación por convocatoria");
            check(scalar("SELECT COUNT(*) FROM pago WHERE postulacion_id="+post)==1,"un pago generado durable");
            check(scalar("SELECT COUNT(*) FROM postulacion_historial WHERE postulacion_id="+post)==1,"historial inicial atómico sin duplicar");
            check(scalar("SELECT estado_actual_id FROM postulacion WHERE id="+post)==2,"estado final enviado por cliente no se acepta");
            check(scalar("SELECT sede_id FROM postulacion WHERE id="+post)==sede,"sede elegida persistida");
            var archivos=new ArchivoBOImpl();ArchivoDigital dni=archivos.guardar(personal("DNI"));
            check(Arrays.equals(PNG,Base64.getDecoder().decode(new ArchivoBOImpl().buscarPorId(dni.getId()).getContenidoBase64())),"bytes DNI recuperables desde otra instancia BO");
            var futura=personal("FOTOGRAFIA");futura.setFechaRegistro(LocalDate.now(ZoneId.of("America/Lima")).plusDays(1));fallo=false;try{archivos.guardar(futura);}catch(IllegalArgumentException expected){fallo=true;}check(fallo,"fecha futura rechazada");
            int pago=scalar("SELECT id FROM pago WHERE postulacion_id="+post);var boPago=new PagoBOImpl();Pago pagoActual=boPago.buscarPorId(pago);pagoActual.setEstadoPago(EstadoPago.APROBADO);fallo=false;try{boPago.modificar(pagoActual);}catch(RuntimeException expected){fallo=true;}check(fallo,"pago sin voucher no se aprueba");
            ArchivoDigital voucher=personal("VOUCHER");voucher.setReferenciaId(pago);voucher.setCodigoOperacion("LAB12-"+post);voucher.setMonto(pagoActual.getMonto());archivos.guardarVoucher(voucher);
            check(scalar("SELECT COUNT(*) FROM pago WHERE id="+pago+" AND estado_pago='PENDIENTE'")==1,"voucher real deja pago pendiente");
            var gate=new CyclicBarrier(2);ExecutorService revision=Executors.newFixedThreadPool(2);
            final int voucherId=voucher.getId();
            var aprobar=revision.submit(()->{gate.await();try{Pago x=new PagoBOImpl().buscarPorId(pago);x.setEstadoPago(EstadoPago.APROBADO);new PagoBOImpl().modificar(x);return true;}catch(RuntimeException expected){return false;}});
            var retirar=revision.submit(()->{gate.await();try{new ArchivoBOImpl().eliminar(voucherId);return true;}catch(RuntimeException expected){return false;}});
            boolean aprobo=aprobar.get(30,TimeUnit.SECONDS),retiro=retirar.get(30,TimeUnit.SECONDS);revision.shutdownNow();
            check(aprobo^retiro,"aprobación y retiro concurrentes tienen un solo efecto válido");
            check(!aprobo||scalar("SELECT COUNT(*) FROM archivo_digital WHERE id="+voucherId+" AND activo=1")==1,"nunca se aprueba un voucher retirado");
            if(retiro){voucher.setId(0);archivos.guardarVoucher(voucher);pagoActual=boPago.buscarPorId(pago);pagoActual.setEstadoPago(EstadoPago.APROBADO);boPago.modificar(pagoActual);}
            pagoActual=boPago.buscarPorId(pago);
            check(scalar("SELECT COUNT(*) FROM pago WHERE id="+pago+" AND estado_pago='APROBADO' AND fecha_pago IS NOT NULL")==1,"aprobación con fecha");
            pagoActual.setEstadoPago(EstadoPago.RECHAZADO);fallo=false;try{boPago.modificar(pagoActual);}catch(RuntimeException expected){fallo=true;}check(fallo,"pago aprobado inmutable");
            check(scalar("SELECT estado_actual_id FROM postulacion WHERE id="+post)==3,"pago aprobado avanza expediente en misma transacción");
            var docsBo=new pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl();
            var docs=docsBo.listarTodos().stream().filter(d->d.getPostulacion().getId()==post).toList();
            var primero=docs.get(0);var docArchivo=archivos.listarPorPostulante(persona).stream().filter(f->f.getTipo().equals("DOCUMENTO")&&f.getReferenciaId()==primero.getId()).findFirst().orElseThrow();
            var restaurar=archivos.buscarPorId(docArchivo.getId());archivos.eliminar(docArchivo.getId());
            primero.setEstadoDocumento(pe.edu.pucp.admitu.postulacion.EstadoDocumento.APROBADO);fallo=false;try{docsBo.modificar(primero);}catch(RuntimeException expected){fallo=true;}check(fallo,"documento con archivo retirado no se aprueba");
            archivos.guardar(restaurar);
            var carneBo=new pe.edu.pucp.admitu.postulacion.bo.CarnePostulanteBOImpl();var carne=new CarnePostulante();
            var expediente=new PostulacionBOImpl().buscarPorId(post);carne.setPostulacion(expediente);Sede lugar=new Sede();lugar.setId(sede);carne.setSede(lugar);carne.setCodigoCarne("LAB12-"+post);carne.setFechaGeneracion(LocalDate.now(ZoneId.of("America/Lima")));carne.setFechaInicioVigencia(carne.getFechaGeneracion());carne.setFechaFinVigencia(carne.getFechaGeneracion().plusMonths(1));carne.setAulaExamen("LAB12-AULA");
            fallo=false;try{carneBo.insertar(carne);}catch(RuntimeException expected){fallo=true;}check(fallo,"carné sin expediente apto rechazado");
            for(var documento:docsBo.listarTodos().stream().filter(d->d.getPostulacion().getId()==post).toList()){documento.setEstadoDocumento(pe.edu.pucp.admitu.postulacion.EstadoDocumento.APROBADO);documento.setFechaEvaluacion(LocalDate.now(ZoneId.of("America/Lima")));docsBo.modificar(documento);}
            expediente.getEstadoActual().setId(5);new PostulacionBOImpl().modificar(expediente);int carneId=carneBo.insertar(carne);
            check(carneId>0&&scalar("SELECT estado_actual_id FROM postulacion WHERE id="+post)==6,"carné apto finaliza expediente de forma atómica");
            carneBo.eliminar(carneId);
            check(carneBo.buscarPorId(carneId)!=null&&"ANULADO".equals(carneBo.buscarPorId(carneId).getAulaExamen()),"anular carné conserva fila, código y titular");
            check(scalar("SELECT COUNT(*) FROM postulacion_historial WHERE postulacion_id="+post)>=4,"la anulación conserva historial del expediente");
            System.out.println("RESULTADO: "+checks+" comprobaciones aprobadas; fixtures se eliminan en finally.");
        }finally{cleanup();}
    }
}
