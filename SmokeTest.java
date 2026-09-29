import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.InstitucionEducativa;
import pe.edu.pucp.admitu.persona.TipoInstitucion;
import pe.edu.pucp.admitu.persona.Apoderado;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.Parentesco;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;
import pe.edu.pucp.admitu.persona.bo.PaisBOImpl;
import pe.edu.pucp.admitu.persona.bo.InstitucionEducativaBOImpl;
import pe.edu.pucp.admitu.persona.bo.ApoderadoBOImpl;
import pe.edu.pucp.admitu.persona.bo.EvaluadorBOImpl;
import pe.edu.pucp.admitu.persona.bo.AntecedenteAcademicoBOImpl;
import pe.edu.pucp.admitu.persona.bo.PostulanteBOImpl;
import pe.edu.pucp.admitu.configuracion.Etapa;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.configuracion.TipoArchivo;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.OfertaCarrera;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaEtapa;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.bo.EtapaBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.ModalidadBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.RequisitoBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.SedeBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.OfertaCarreraBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaEtapaBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaModalidadBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.RequisitoConvocatoriaModalidadBOImpl;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.CarnePostulante;
import pe.edu.pucp.admitu.postulacion.EstadoDocumento;
import pe.edu.pucp.admitu.postulacion.EstadoObservacion;
import pe.edu.pucp.admitu.postulacion.TipoObservacion;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.EstadoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionHistorialBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoObservacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.CarnePostulanteBOImpl;
import pe.edu.pucp.admitu.notificacion.Notificacion;
import pe.edu.pucp.admitu.notificacion.MedioNotificacion;
import pe.edu.pucp.admitu.notificacion.TipoNotificacion;
import pe.edu.pucp.admitu.notificacion.EstadoEnvio;
import pe.edu.pucp.admitu.notificacion.bo.NotificacionBOImpl;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.MedioPago;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;

import java.time.LocalDate;

public class SmokeTest {
    static int ok = 0, fail = 0;
    static void chk(String paso, boolean cond) {
        if (cond) { ok++; System.out.println("  OK   " + paso); }
        else { fail++; System.out.println("  FAIL " + paso); }
    }
    public static void main(String[] a) {
        int s = (int) (System.currentTimeMillis() % 100000);
        String correo = "smoke" + s + "@admitu.pe";

        PaisBOImpl paisBO = new PaisBOImpl();
        Pais pais = new Pais("Z" + (char)('A'+s%20), "Pais Smoke " + s);
        int paisId = paisBO.insertar(pais);
        chk("PAIS insertar devuelve id", paisId > 0);
        chk("PAIS id propagado al objeto", pais.getId() == paisId);
        Pais paisLeido = paisBO.buscarPorId(paisId);
        chk("PAIS buscarPorId conserva id de BD", paisLeido != null && paisLeido.getId() == paisId);
        chk("PAIS listarTodos", paisBO.listarTodos() != null);
        pais.setNombre("Pais Smoke MOD " + s);
        chk("PAIS modificar", paisBO.modificar(pais) > 0);
        chk("PAIS baja logica", paisBO.eliminar(paisId) > 0);

        InstitucionEducativaBOImpl instBO = new InstitucionEducativaBOImpl();
        InstitucionEducativa inst = new InstitucionEducativa(paisLeido, "EXT" + s, "Inst Smoke " + s, TipoInstitucion.UNIVERSIDAD);
        int instId = instBO.insertar(inst);
        chk("INSTITUCION insertar", instId > 0);
        chk("INSTITUCION id en objeto", inst.getId() == instId);
        InstitucionEducativa instLeida = instBO.buscarPorId(instId);
        chk("INSTITUCION mapear pais", instLeida.getPais() != null && instLeida.getPais().getId() == paisId);
        chk("INSTITUCION mapear id propio", instLeida.getId() == instId);

        ApoderadoBOImpl apodBO = new ApoderadoBOImpl();
        Apoderado apod = new Apoderado("Apoderado"+s, "Paterno", "Materno", "apod"+s+"@admitu.pe", TipoDocumento.DNI, String.valueOf(70000000L+s), "999888777", Parentesco.PADRE, null);
        int apodId = apodBO.insertar(apod);
        chk("APODERADO insertar (persona+apoderado)", apodId > 0);
        Apoderado apodLeido = apodBO.buscarPorId(apodId);
        chk("APODERADO mapear parentesco", apodLeido.getParentesco() == Parentesco.PADRE);
        chk("APODERADO id de BD", apodLeido.getId() == apodId);
        chk("APODERADO listarTodos", apodBO.listarTodos() != null);

        EvaluadorBOImpl evalBO = new EvaluadorBOImpl();
        Evaluador ev = new Evaluador("Evaluador"+s, "Paterno", "Materno", "eval"+s+"@admitu.pe", TipoDocumento.DNI, String.valueOf(80000000L+s), "999777666", "Docente");
        int evalId = evalBO.insertar(ev);
        chk("EVALUADOR insertar", evalId > 0);
        Evaluador evLeido = evalBO.buscarPorId(evalId);
        chk("EVALUADOR mapear cargo", "Docente".equals(evLeido.getCargo()));
        chk("EVALUADOR id de BD", evLeido.getId() == evalId);

        PostulanteBOImpl postBO = new PostulanteBOImpl();
        Postulante post = new Postulante("Postulante"+s, "Paterno", "Materno", correo, TipoDocumento.DNI, String.valueOf(90000000L+s), "911222333", null, LocalDate.of(2006,5,4), false, null, false, null, null, null, null);
        int postId = postBO.insertar(post);
        chk("POSTULANTE insertar", postId > 0);

        AntecedenteAcademicoBOImpl antBO = new AntecedenteAcademicoBOImpl();
        AntecedenteAcademico ant = new AntecedenteAcademico(postLeido(postId), instLeida, 2019, 2023, "Smoke " + s);
        int antId = antBO.insertar(ant);
        chk("ANTECEDENTE insertar", antId > 0);
        AntecedenteAcademico antLeido = antBO.buscarPorId(antId);
        chk("ANTECEDENTE mapear institucion", antLeido.getInstitucion() != null && antLeido.getInstitucion().getId() == instId);
        chk("ANTECEDENTE mapear postulante", antLeido.getPostulante() != null && antLeido.getPostulante().getId() == postId);
        chk("ANTECEDENTE id de BD", antLeido.getId() == antId);

        EtapaBOImpl etapaBO = new EtapaBOImpl();
        Etapa etapa = new Etapa(LocalDate.of(2026,1,5), LocalDate.of(2026,2,5), "ES"+s, "Etapa Smoke", "Etapa smoke");
        int etapaId = etapaBO.insertar(etapa);
        chk("ETAPA insertar", etapaId > 0);
        Etapa etapaLeida = etapaBO.buscarPorId(etapaId);
        chk("ETAPA id de BD", etapaLeida.getId() == etapaId);

        ModalidadBOImpl modBO = new ModalidadBOImpl();
        Modalidad mod = new Modalidad("MS"+s, "Modalidad Smoke", "mod smoke", false, true);
        int modId = modBO.insertar(mod);
        chk("MODALIDAD insertar", modId > 0);
        chk("MODALIDAD id de BD", modBO.buscarPorId(modId).getId() == modId);

        RequisitoBOImpl reqBO = new RequisitoBOImpl();
        Requisito req = new Requisito("RS"+s, "Requisito Smoke", "req smoke", TipoArchivo.PDF, 5242880);
        int reqId = reqBO.insertar(req);
        chk("REQUISITO insertar", reqId > 0);
        chk("REQUISITO mapear tipo archivo", reqBO.buscarPorId(reqId).getTipoArchivoRequerido() == TipoArchivo.PDF);

        SedeBOImpl sedeBO = new SedeBOImpl();
        Sede sede = new Sede("SS"+s, "Sede Smoke", "Av. Smoke 123");
        int sedeId = sedeBO.insertar(sede);
        chk("SEDE insertar", sedeId > 0);
        chk("SEDE id de BD", sedeBO.buscarPorId(sedeId).getId() == sedeId);

        EstadoPostulacionBOImpl estBO = new EstadoPostulacionBOImpl();
        EstadoPostulacion est = new EstadoPostulacion("ESM"+s, "En Evaluacion Smoke", "estado smoke");
        int estId = estBO.insertar(est);
        chk("ESTADO insertar", estId > 0);
        chk("ESTADO id de BD", estBO.buscarPorId(estId).getId() == estId);

        ConvocatoriaBOImpl convBO = new ConvocatoriaBOImpl();
        Convocatoria conv = new Convocatoria();
        conv.setCodigo("CV"+s); conv.setNombre("Convocatoria Smoke"); conv.setPeriodo("2026-0"+s%9);
        conv.setFechaInicio(LocalDate.of(2026,1,1)); conv.setFechaFin(LocalDate.of(2026,3,31));
        conv.setEstado("BORRADOR"); conv.setDescripcion("conv smoke");
        int convId = convBO.insertar(conv);
        chk("CONVOCATORIA insertar", convId > 0);

        OfertaCarreraBOImpl ofBO = new OfertaCarreraBOImpl();
        OfertaCarrera oferta = new OfertaCarrera(convLeido(convId), firstCarrera(), 25);
        int ofertaId = ofBO.insertar(oferta);
        chk("OFERTA insertar", ofertaId > 0);
        OfertaCarrera ofLeida = ofBO.buscarPorId(ofertaId);
        chk("OFERTA mapear carrera", ofLeida.getCarrera() != null && ofLeida.getCarrera().getId() > 0);
        chk("OFERTA mapear convocatoria", ofLeida.getConvocatoria() != null && ofLeida.getConvocatoria().getId() == convId);
        chk("OFERTA id de BD", ofLeida.getId() == ofertaId);

        ConvocatoriaEtapaBOImpl ceBO = new ConvocatoriaEtapaBOImpl();
        ConvocatoriaEtapa ce = new ConvocatoriaEtapa(convLeido(convId), etapaLeida, LocalDate.of(2026,1,5), LocalDate.of(2026,1,20));
        int ceId = ceBO.insertar(ce);
        chk("CONV_ETAPA insertar", ceId > 0);
        ConvocatoriaEtapa ceLeida = ceBO.buscarPorId(ceId);
        chk("CONV_ETAPA mapear etapa con id", ceLeida.getEtapa() != null && ceLeida.getEtapa().getId() == etapaId);
        chk("CONV_ETAPA id de BD", ceLeida.getId() == ceId);

        ConvocatoriaModalidadBOImpl cmBO = new ConvocatoriaModalidadBOImpl();
        ConvocatoriaModalidad cm = new ConvocatoriaModalidad(convLeido(convId), modBO.buscarPorId(modId), 150.50, "obs smoke", null);
        int cmId = cmBO.insertar(cm);
        chk("CONV_MODALIDAD insertar", cmId > 0);
        ConvocatoriaModalidad cmLeida = cmBO.buscarPorId(cmId);
        chk("CONV_MODALIDAD mapear modalidad con id", cmLeida.getModalidad() != null && cmLeida.getModalidad().getId() == modId);
        chk("CONV_MODALIDAD costo", cmLeida.getCostoInscripcion() == 150.50);
        chk("CONV_MODALIDAD id de BD", cmLeida.getId() == cmId);

        RequisitoConvocatoriaModalidadBOImpl rcmBO = new RequisitoConvocatoriaModalidadBOImpl();
        RequisitoConvocatoriaModalidad rcm = new RequisitoConvocatoriaModalidad(cmLeida, reqBO.buscarPorId(reqId), true, 1);
        int rcmId = rcmBO.insertar(rcm);
        chk("REQ_CONV_MOD insertar", rcmId > 0);
        RequisitoConvocatoriaModalidad rcmLeido = rcmBO.buscarPorId(rcmId);
        chk("REQ_CONV_MOD mapear requisito con id", rcmLeido.getRequisito() != null && rcmLeido.getRequisito().getId() == reqId);
        chk("REQ_CONV_MOD mapear orden", Integer.valueOf(1).equals(rcmLeido.getOrdenPresentacion()));
        chk("REQ_CONV_MOD obligatorio", rcmLeido.isObligatorio());
        chk("REQ_CONV_MOD id de BD", rcmLeido.getId() == rcmId);

        PostulacionBOImpl postulBO = new PostulacionBOImpl();
        Postulacion postul = new Postulacion(postLeido(postId), convLeido(convId), cmLeida, ofLeida, estBO.buscarPorId(estId), "SMK"+s, "postul smoke", LocalDate.of(2026,1,10), null, null);
        int postulId = postulBO.insertar(postul);
        chk("POSTULACION insertar", postulId > 0);
        Postulacion postulLeida = postulBO.buscarPorId(postulId);
        chk("POSTULACION mapear estado con id", postulLeida.getEstadoActual() != null && postulLeida.getEstadoActual().getId() == estId);
        chk("POSTULACION id de BD", postulLeida.getId() == postulId);

        PostulacionHistorialBOImpl histBO = new PostulacionHistorialBOImpl();
        PostulacionHistorial hist = new PostulacionHistorial(postulLeida, null, estBO.buscarPorId(estId), LocalDate.of(2026,1,11), "coord smoke", "motivo smoke");
        int histId = histBO.insertar(hist);
        chk("HISTORIAL insertar", histId > 0);
        PostulacionHistorial histLeido = histBO.buscarPorId(histId);
        chk("HISTORIAL estado anterior null", histLeido.getEstadoAnterior() == null);
        chk("HISTORIAL mapear estado actual con id", histLeido.getEstadoActual() != null && histLeido.getEstadoActual().getId() == estId);
        chk("HISTORIAL id de BD", histLeido.getId() == histId);

        DocumentoPostulacionBOImpl docBO = new DocumentoPostulacionBOImpl();
        DocumentoPostulacion doc = new DocumentoPostulacion(postulLeida, reqBO.buscarPorId(reqId), 1, "cv.pdf", TipoArchivo.PDF, 102400L, "/smoke/cv.pdf", LocalDate.of(2026,1,12), EstadoDocumento.OBSERVADO, LocalDate.of(2026,1,13), "comentario smoke", null);
        int docId = docBO.insertar(doc);
        chk("DOC_POSTULACION insertar", docId > 0);
        DocumentoPostulacion docLeido = docBO.buscarPorId(docId);
        chk("DOC_POSTULACION mapear requisito con id", docLeido.getRequisitoAplicable() != null && docLeido.getRequisitoAplicable().getId() == reqId);
        chk("DOC_POSTULACION id de BD", docLeido.getId() == docId);

        DocumentoObservacionBOImpl obsBO = new DocumentoObservacionBOImpl();
        DocumentoObservacion obs = new DocumentoObservacion(docLeido, evBO.buscarPorId(evalId), TipoObservacion.OTRO, "obs smoke", LocalDate.of(2026,1,14), EstadoObservacion.PENDIENTE, null, null);
        int obsId = obsBO.insertar(obs);
        chk("DOC_OBSERVACION insertar", obsId > 0);
        DocumentoObservacion obsLeida = obsBO.buscarPorId(obsId);
        chk("DOC_OBS mapear documento con id", obsLeida.getDocumento() != null && obsLeida.getDocumento().getId() == docId);
        chk("DOC_OBS mapear evaluador con id", obsLeida.getEvaluador() != null && obsLeida.getEvaluador().getId() == evalId);
        chk("DOC_OBSERVACION id de BD", obsLeida.getId() == obsId);

        CarnePostulanteBOImpl carneBO = new CarnePostulanteBOImpl();
        CarnePostulante carne = new CarnePostulante(postulLeida, sedeBO.buscarPorId(sedeId), "CAR"+s, LocalDate.of(2026,1,15), LocalDate.of(2026,1,15), LocalDate.of(2026,6,15), "A-101");
        int carneId = carneBO.insertar(carne);
        chk("CARNE insertar", carneId > 0);
        CarnePostulante carneLeida = carneBO.buscarPorId(carneId);
        chk("CARNE mapear sede con id", carneLeida.getSede() != null && carneLeida.getSede().getId() == sedeId);
        chk("CARNE id de BD", carneLeida.getId() == carneId);

        NotificacionBOImpl notifBO = new NotificacionBOImpl();
        Notificacion notif = new Notificacion(postulLeida, obsLeida, MedioNotificacion.CORREO, TipoNotificacion.OBSERVACION, "dest"+s+"@admitu.pe", "Observacion smoke", "mensaje smoke", LocalDate.of(2026,1,16), null, EstadoEnvio.PENDIENTE, false, null);
        int notifId = notifBO.insertar(notif);
        chk("NOTIFICACION insertar", notifId > 0);
        Notificacion notifLeida = notifBO.buscarPorId(notifId);
        chk("NOTIF mapear observacion con id", notifLeida.getObservacionOrigen() != null && notifLeida.getObservacionOrigen().getId() == obsId);
        chk("NOTIF mapear postulacion con id", notifLeida.getPostulacion() != null && notifLeida.getPostulacion().getId() == postulId);
        chk("NOTIFICACION id de BD", notifLeida.getId() == notifId);
        chk("NOTIF listarTodos", notifBO.listarTodos() != null);

        PagoBOImpl pagoBO = new PagoBOImpl();
        Pago pago = new Pago(postulLeida, MedioPago.TRANSFERENCIA, 150.50, LocalDate.of(2026,1,10), null, "PG"+s, "REF"+s, EstadoPago.GENERADO, "/smoke/voucher.png");
        chk("PAGO insertar", pagoBO.insertar(pago) > 0);

        chk("validacion BO: pais null", falla(() -> paisBO.insertar(null)));
        chk("validacion BO: iso2 invalido", falla(() -> paisBO.insertar(new Pais("XYZ", "Nombre largo"))));
        chk("validacion BO: etapa con fechas invertidas", falla(() -> etapaBO.insertar(new Etapa(LocalDate.of(2026,5,1), LocalDate.of(2026,1,1), "X"+s, "M", "d"))));
        chk("validacion BO: vacantes negativas", falla(() -> ofBO.insertar(new OfertaCarrera(convLeido(convId), firstCarrera(), -3))));
        chk("validacion BO: antecedente con anios invertidos", falla(() -> antBO.insertar(new AntecedenteAcademico(postLeido(postId), instLeida, 2023, 2019, "x"))));

        new DocumentoObservacionBOImpl().eliminar(obsId);
        chk("ELIMINAR_DOCUMENTO_OBSERVACION encadena notificacion", notifBO.buscarPorId(notifId) == null);
        new DocumentoPostulacionBOImpl().eliminar(docId);
        chk("ELIMINAR_DOCUMENTO_POSTULACION", new DocumentoPostulacionBOImpl().buscarPorId(docId) == null);
        postulBO.eliminar(postulId);
        chk("ELIMINAR_POSTULACION encadena toda la jerarquia", postulBO.buscarPorId(postulId) == null);
        chk("ELIMINAR_POSTULACION borro el carne", new CarnePostulanteBOImpl().buscarPorId(carneId) == null);
        chk("ELIMINAR_POSTULACION borro el historial", new PostulacionHistorialBOImpl().buscarPorId(histId) == null);

        System.out.println("\n== resultado: " + ok + " OK / " + fail + " FAIL ==");
        if (fail > 0) System.exit(1);
    }
    interface Bloque { void run(); }
    static boolean falla(Bloque b) { try { b.run(); return false; } catch (RuntimeException e) { return true; } }
    static Postulante postLeido(int id) { return new PostulanteBOImpl().buscarPorId(id); }
    static Convocatoria convLeido(int id) { return new ConvocatoriaBOImpl().buscarPorId(id); }
    static pe.edu.pucp.admitu.configuracion.Carrera firstCarrera() { return new pe.edu.pucp.admitu.configuracion.bo.CarreraBOImpl().listarTodos().get(0); }
}
