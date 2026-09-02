import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Principal {
    public static void main(String[] args) {
        // CATALOGOS
        Pais peru = new Pais("PE", "Peru");
        Pais chile = new Pais("CL", "Chile");

        Facultad ing = new Facultad("FING", "Ingenieria");
        Facultad admin = new Facultad("FADM", "Administracion");

        Carrera sistemas = new Carrera(ing, "C01", "Ingenieria de Sistemas");
        Carrera civil = new Carrera(ing, "C02", "Ingenieria Civil");
        Carrera contabilidad = new Carrera(admin, "C03", "Contabilidad");

        Sede lima = new Sede("S01", "Lima", "Av. Universitaria 1801");
        Sede arequipa = new Sede("S02", "Arequipa", "Av. La Paz 600");

        Modalidad ordinario = new Modalidad("MO", "Ordinario", "Examen de admision general", false, false);
        Modalidad cepre = new Modalidad("CP", "CEPRU", "Centro Preuniversitario", true, false);

        Etapa inscripcion = new Etapa(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30), "E1", "Inscripcion", "Periodo de registro");
        Etapa examen = new Etapa(LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 12), "E2", "Examen", "Aplicacion de la prueba");

        InstitucionEducativa colegio = new InstitucionEducativa(peru, "COL-101", "Colegio Nacional Mixto", TipoInstitucion.COLEGIO);
        InstitucionEducativa univ = new InstitucionEducativa(peru, "UNI-200", "Universidad Nacional", TipoInstitucion.UNIVERSIDAD);

        // REQUISITOS Y MEDIOS
        Requisito docIdentidad = new Requisito("R01", "Documento de identidad", "Copia del DNI vigente", TipoArchivo.IMAGEN, 5242880);
        Requisito foto = new Requisito("R02", "Foto tamano carnet", "Foto a color fondo blanco", TipoArchivo.IMAGEN, 2097152);
        Requisito certNotas = new Requisito("R03", "Certificado de notas", "Certificado del colegio", TipoArchivo.PDF, 5242880);

        MedioPago tarjeta = new MedioPago("VIS", "Visa", true);
        MedioPago yape = new MedioPago("YAP", "Yape", true);
        MedioPago banco = new MedioPago("BCP", "Banco de Credito", false);

        EstadoPostulacion borrador = new EstadoPostulacion("BORRADOR", "Borrador", "Postulacion en edicion");
        EstadoPostulacion completada = new EstadoPostulacion("COMPLETADA", "Completada", "Lista para enviar");
        EstadoPostulacion enProceso = new EstadoPostulacion("EN_PROCESO", "En Proceso", "Bajo evaluacion");

        // CONVOCATORIA
        List<Postulante> postulantesIniciales = new ArrayList<>();
        List<ConvocatoriaModalidad> modalidades = new ArrayList<>();
        List<OfertaCarrera> ofertas = new ArrayList<>();
        List<ConvocatoriaEtapa> etapas = new ArrayList<>();

        Convocatoria convocatoria = new Convocatoria("ADM-2026", "Admision 2026", "2026-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 30), EstadoConvocatoria.PUBLICADA,
                "Proceso de admision ordinario", postulantesIniciales, modalidades, ofertas, etapas);

        ConvocatoriaModalidad convOrdinaria = new ConvocatoriaModalidad(convocatoria, ordinario, 150.00, "Sin descuento", new ArrayList<>());
        ConvocatoriaModalidad convCepre = new ConvocatoriaModalidad(convocatoria, cepre, 120.00, "Descuento cepru", new ArrayList<>());

        RequisitoConvocatoriaModalidad reqDni = new RequisitoConvocatoriaModalidad(convOrdinaria, docIdentidad, true, 1);
        RequisitoConvocatoriaModalidad reqFoto = new RequisitoConvocatoriaModalidad(convOrdinaria, foto, true, 2);
        RequisitoConvocatoriaModalidad reqNotas = new RequisitoConvocatoriaModalidad(convCepre, certNotas, false, 3);
        List<RequisitoConvocatoriaModalidad> reqsOrd = new ArrayList<>();
        reqsOrd.add(reqDni);
        reqsOrd.add(reqFoto);
        convOrdinaria.setRequisitos(reqsOrd);

        OfertaCarrera ofSistemas = new OfertaCarrera(convocatoria, sistemas, 50);
        OfertaCarrera ofCivil = new OfertaCarrera(convocatoria, civil, 30);
        ofertas.add(ofSistemas);
        ofertas.add(ofCivil);
        convocatoria.setCarrerasOfrecidas(ofertas);

        ConvocatoriaEtapa ceInscripcion = new ConvocatoriaEtapa(convocatoria, inscripcion, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30));
        ConvocatoriaEtapa ceExamen = new ConvocatoriaEtapa(convocatoria, examen, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 12));
        etapas.add(ceInscripcion);
        etapas.add(ceExamen);
        convocatoria.setEtapas(etapas);

        // POSTULANTE, APODERADO, EVALUADOR
        Apoderado apoderado = new Apoderado("Carlos", "Gomez", "Ruiz", "carlos.gomez@mail.com",
                TipoDocumento.DNI, "40112233", "999111222", Parentesco.PADRE, new ArrayList<>());

        Postulante postulante = new Postulante("Maria", "Perez", "Lopez", "maria.perez@mail.com",
                TipoDocumento.DNI, "70223344", "988776655", apoderado, LocalDate.of(2006, 5, 14),
                false, null, true, LocalDate.of(2026, 2, 20), new ArrayList<>(), new ArrayList<>());

        Apoderado apoderado2 = new Apoderado("Luis", "Rojas", "Delgado", "luis.rojas@mail.com",
                TipoDocumento.DNI, "40556677", "922333444", Parentesco.TUTOR, new ArrayList<>());
        List<Postulante> apP = new ArrayList<>();
        apP.add(postulante);
        apoderado2.setPostulantes(apP);

        Evaluador evaluador = new Evaluador("Ana", "Torres", "Vega", "ana.torres@admis.univ.edu.pe",
                TipoDocumento.DNI, "41122334", "911444555", "Jefa de Evaluadores");

        // POSTULACION, PAGO, CARNE
        List<PostulacionHistorial> historial = new ArrayList<>();
        List<Pago> pagos = new ArrayList<>();
        List<DocumentoPostulacion> documentos = new ArrayList<>();
        List<Notificacion> notificaciones = new ArrayList<>();

        Postulacion postulacion = new Postulacion(postulante, convocatoria, convOrdinaria, ofSistemas,
                borrador, LocalDate.of(2026, 3, 5), null, null, "INS-2026-0001",
                "Postulacion inicial", historial, pagos, documentos, notificaciones, null);

        PostulacionHistorial h1 = new PostulacionHistorial(postulacion, borrador, completada,
                LocalDate.of(2026, 3, 6), "Sistema", "El postulante completo los datos");
        historial.add(h1);
        postulacion.setHistorial(historial);
        postulacion.setEstadoActual(completada);

        Pago pago = new Pago(postulacion, tarjeta, 1, "PAG-2026-0001", "TF-88231",
                LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 6), 150.00, 0.0, 150.00,
                EstadoPago.APROBADO, LocalDate.of(2026, 3, 6), "Mostrador", "Pago aprobado automaticamente");
        pagos.add(pago);
        postulacion.setPagos(pagos);

        postulacion.setFechaEnvio(LocalDate.of(2026, 3, 7));
        postulacion.setEstadoActual(enProceso);

        CarnePostulante carne = new CarnePostulante(postulacion, lima, "CAR-001",
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 12), "Aula 302");
        postulacion.setCarne(carne);

        // DOCUMENTOS, OBSERVACIONES, NOTIFICACIONES
        DocumentoPostulacion docDni = new DocumentoPostulacion(postulacion, docIdentidad, 1,
                "dni_maria.jpg", TipoArchivo.IMAGEN, 102400L, "/docs/dni_maria.jpg",
                LocalDate.of(2026, 3, 7), EstadoDocumento.PENDIENTE, null, null, new ArrayList<>());
        documentos.add(docDni);
        postulacion.setDocumentos(documentos);

        DocumentoObservacion obs = new DocumentoObservacion(docDni, evaluador, TipoObservacion.ILEGIBLE,
                "La foto del DNI se ve borrosa", LocalDate.of(2026, 3, 8),
                EstadoObservacion.PENDIENTE, null, null);
        List<DocumentoObservacion> obsList = new ArrayList<>();
        obsList.add(obs);
        docDni.setObservaciones(obsList);
        docDni.setEstadoDocumento(EstadoDocumento.OBSERVADO);

        Notificacion notif = new Notificacion(postulacion, obs, MedioNotificacion.EMAIL,
                TipoNotificacion.OBSERVACION, "maria.perez@mail.com", "Documento observado",
                "Su DNI fue observado, vuelva a cargarlo", LocalDate.of(2026, 3, 8),
                LocalDate.of(2026, 3, 8), EstadoEnvio.ENVIADA);
        notificaciones.add(notif);
        postulacion.setNotificaciones(notificaciones);

        // SUBSANACION
        obs.setEstadoObservacion(EstadoObservacion.SUBSANADA);
        obs.setFechaSubsanacion(LocalDate.of(2026, 3, 9));
        obs.setComentarioSubsanacion("Reemplazado por foto legible");
        docDni.setEstadoDocumento(EstadoDocumento.APROBADO);
        docDni.setFechaEvaluacion(LocalDate.of(2026, 3, 9));
        docDni.setComentarioEvaluacion("Documento corregido y aceptado");

        // SALIDA DEL FLUJO
        System.out.println("=== FLUJO ADMITU ===");
        System.out.println("Postulante: " + postulante.getNombres() + " " + postulante.getApellidoPaterno());
        System.out.println("Convocatoria: " + convocatoria.getNombre() + " (" + convocatoria.getPeriodo() + ")");
        System.out.println("Modalidad: " + postulacion.getModalidadElegida().getModalidad().getNombre());
        System.out.println("Carrera: " + postulacion.getCarreraElegida().getCarrera().getNombre());
        System.out.println("Estado: " + postulacion.getEstadoActual().getNombre());
        System.out.println("Inscripcion: " + postulacion.getCodigoInscripcion());
        System.out.println("Pago: S/ " + pago.getMontoPagado() + " [" + pago.getEstadoPago() + "]");
        System.out.println("Documentos: " + postulacion.getDocumentos().size() + " (DNI: " + docDni.getEstadoDocumento() + ")");
        System.out.println("Notificaciones: " + postulacion.getNotificaciones().size());
        System.out.println("Carne: " + carne.getCodigoCarne() + " en " + carne.getSede().getNombre() + " (" + carne.getAulaExamen() + ")");
    }
}