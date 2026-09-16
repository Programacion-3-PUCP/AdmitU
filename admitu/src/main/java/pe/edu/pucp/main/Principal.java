package pe.edu.pucp.main;

import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.notificacion.EstadoEnvio;
import pe.edu.pucp.admitu.notificacion.MedioNotificacion;
import pe.edu.pucp.admitu.notificacion.Notificacion;
import pe.edu.pucp.admitu.notificacion.TipoNotificacion;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.pago.MedioPago;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.persona.*;
import pe.edu.pucp.admitu.postulacion.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Principal {
    public static void main(String[] args) {
        // pais
        Pais peru = new Pais("PE", "Peru");

        // facultades
        Facultad ing = new Facultad("FING", "Ingenieria");
        Facultad admin = new Facultad("FADM", "Administracion");

        // carreras
        Carrera sistemas = new Carrera(ing, "C01", "Ingenieria de Sistemas");
        Carrera civil = new Carrera(ing, "C02", "Ingenieria Civil");
        Carrera contabilidad = new Carrera(admin, "C03", "Contabilidad");

        // sedes
        Sede lima = new Sede("S01", "Lima", "Av. Universitaria 1801");
        Sede arequipa = new Sede("S02", "Arequipa", "Av. La Paz 600");

        // modalidades
        Modalidad ordinario = new Modalidad("MO", "Ordinario", "Examen de admision general", false, false);
        Modalidad cepre = new Modalidad("CP", "CEPRU", "Centro Preuniversitario", true, false);

        // etapas
        Etapa inscripcion = new Etapa(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30), "E1", "Inscripcion", "Periodo de registro");
        Etapa examen = new Etapa(LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 12), "E2", "Examen", "Aplicacion de la prueba");

        // colegio
        InstitucionEducativa colegio = new InstitucionEducativa(peru, "COL-101", "Colegio Nacional Mixto", TipoInstitucion.COLEGIO);

        // requisitos
        Requisito docIdentidad = new Requisito("R01", "Documento de identidad", "Copia del DNI vigente", TipoArchivo.IMAGEN, 5242880);
        Requisito foto = new Requisito("R02", "Foto tamano carnet", "Foto a color fondo blanco", TipoArchivo.IMAGEN, 2097152);
        Requisito certNotas = new Requisito("R03", "Certificado de notas", "Certificado del colegio", TipoArchivo.PDF, 5242880);

        // medio de pago
        MedioPago tarjeta = MedioPago.TARJETA;

        // estados
        EstadoPostulacion borrador = new EstadoPostulacion("BORRADOR", "Borrador", "pe.edu.pucp.admitu.postulacion.Postulacion en edicion");
        EstadoPostulacion completada = new EstadoPostulacion("COMPLETADA", "Completada", "Lista para enviar");
        EstadoPostulacion enProceso = new EstadoPostulacion("EN_PROCESO", "En Proceso", "Bajo evaluacion");

        // convocatoria
        List<Postulante> postulantesIniciales = new ArrayList<>();
        List<ConvocatoriaModalidad> modalidades = new ArrayList<>();
        List<OfertaCarrera> ofertas = new ArrayList<>();
        List<ConvocatoriaEtapa> etapas = new ArrayList<>();

        Convocatoria convocatoria = new Convocatoria("ADM-2026", "Admision 2026", "2026-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 30), EstadoConvocatoria.PUBLICADA,
                "Proceso de admision ordinario", postulantesIniciales, modalidades, ofertas, etapas);

        // modalidades de la convocatoria
        ConvocatoriaModalidad convOrdinaria = new ConvocatoriaModalidad(convocatoria, ordinario, 150.00, "Sin descuento", new ArrayList<>());
        ConvocatoriaModalidad convCepre = new ConvocatoriaModalidad(convocatoria, cepre, 120.00, "Descuento cepru", new ArrayList<>());

        RequisitoConvocatoriaModalidad reqDni = new RequisitoConvocatoriaModalidad(convOrdinaria, docIdentidad, true, 1);
        RequisitoConvocatoriaModalidad reqFoto = new RequisitoConvocatoriaModalidad(convOrdinaria, foto, true, 2);
        RequisitoConvocatoriaModalidad reqNotas = new RequisitoConvocatoriaModalidad(convCepre, certNotas, false, 3);
        List<RequisitoConvocatoriaModalidad> reqsOrd = new ArrayList<>();
        reqsOrd.add(reqDni);
        reqsOrd.add(reqFoto);
        convOrdinaria.setRequisitos(reqsOrd);

        List<RequisitoConvocatoriaModalidad> reqsCepre = new ArrayList<>();
        reqsCepre.add(reqNotas);
        convCepre.setRequisitos(reqsCepre);
        modalidades.add(convOrdinaria);
        modalidades.add(convCepre);
        convocatoria.setModalidadesHabilitadas(modalidades);

        // carreras ofrecidas
        OfertaCarrera ofSistemas = new OfertaCarrera(convocatoria, sistemas, 50);
        OfertaCarrera ofCivil = new OfertaCarrera(convocatoria, civil, 30);
        ofertas.add(ofSistemas);
        ofertas.add(ofCivil);
        convocatoria.setCarrerasOfrecidas(ofertas);

        // etapas de la convocatoria
        ConvocatoriaEtapa ceInscripcion = new ConvocatoriaEtapa(convocatoria, inscripcion, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30));
        ConvocatoriaEtapa ceExamen = new ConvocatoriaEtapa(convocatoria, examen, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 12));
        etapas.add(ceInscripcion);
        etapas.add(ceExamen);
        convocatoria.setEtapas(etapas);

        // apoderado y postulante
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

        // evaluador
        Evaluador evaluador = new Evaluador("Ana", "Torres", "Vega", "ana.torres@admis.univ.edu.pe",
                TipoDocumento.DNI, "41122334", "911444555", "Jefa de Evaluadores");

        // postulacion
        List<PostulacionHistorial> historial = new ArrayList<>();
        List<Pago> pagos = new ArrayList<>();
        List<DocumentoPostulacion> documentos = new ArrayList<>();
        List<Notificacion> notificaciones = new ArrayList<>();

        Postulacion postulacion = new Postulacion(postulante, convocatoria, convOrdinaria, ofSistemas,
                borrador, LocalDate.of(2026, 3, 5), null, null, "INS-2026-0001",
                "pe.edu.pucp.admitu.postulacion.Postulacion inicial", historial, pagos, documentos, notificaciones, null);

        // historial
        PostulacionHistorial h1 = new PostulacionHistorial(postulacion, borrador, completada,
                LocalDate.of(2026, 3, 6), "Sistema", "El postulante completo los datos");
        historial.add(h1);
        postulacion.setHistorial(historial);
        postulacion.setEstadoActual(completada);

        // pago
        Pago pago = new Pago(postulacion, tarjeta, 150.00,
                LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 6), "PAG-2026-0001", "TF-88231",
                EstadoPago.APROBADO, "/docs/voucher.pdf");
        pagos.add(pago);
        postulacion.setPagos(pagos);

        // envio a evaluacion
        postulacion.setFechaEnvio(LocalDate.of(2026, 3, 7));
        postulacion.setEstadoActual(enProceso);

        // carne
        CarnePostulante carne = new CarnePostulante(postulacion, lima, "CAR-001",
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 12), "Aula 302");
        postulacion.setCarne(carne);

        // documento
        DocumentoPostulacion docDni = new DocumentoPostulacion(postulacion, docIdentidad, 1,
                "dni_maria.jpg", TipoArchivo.IMAGEN, 102400L, "/docs/dni_maria.jpg",
                LocalDate.of(2026, 3, 7), EstadoDocumento.PENDIENTE, null, null, new ArrayList<>());
        documentos.add(docDni);
        postulacion.setDocumentos(documentos);

        // observacion del documento
        DocumentoObservacion obs = new DocumentoObservacion(docDni, evaluador, TipoObservacion.ILEGIBLE,
                "La foto del DNI se ve borrosa", LocalDate.of(2026, 3, 8),
                EstadoObservacion.PENDIENTE, null, null);
        List<DocumentoObservacion> obsList = new ArrayList<>();
        obsList.add(obs);
        docDni.setObservaciones(obsList);
        docDni.setEstadoDocumento(EstadoDocumento.OBSERVADO);

        // notificaciones
        Notificacion notif = new Notificacion(postulacion, obs, MedioNotificacion.CORREO,
                TipoNotificacion.OBSERVACION, "maria.perez@mail.com", "Documento observado",
                "Su DNI fue observado, vuelva a cargarlo", LocalDate.of(2026, 3, 8),
                LocalDate.of(2026, 3, 8), EstadoEnvio.ENVIADA);
        notificaciones.add(notif);
        Notificacion notifBandeja = new Notificacion(postulacion, obs, MedioNotificacion.BANDEJA_SISTEMA,
                TipoNotificacion.OBSERVACION, "maria.perez@mail.com", "Documento observado",
                "Revise su bandeja: DNI observado", LocalDate.of(2026, 3, 8),
                LocalDate.of(2026, 3, 8), EstadoEnvio.ENVIADA);
        notifBandeja.setLeida(true);
        notifBandeja.setFechaLectura(LocalDate.of(2026, 3, 9));
        notificaciones.add(notifBandeja);
        postulacion.setNotificaciones(notificaciones);

        // se subsana lo observado
        obs.setEstadoObservacion(EstadoObservacion.SUBSANADA);
        obs.setFechaSubsanacion(LocalDate.of(2026, 3, 9));
        obs.setComentarioSubsanacion("Reemplazado por foto legible");
        docDni.setEstadoDocumento(EstadoDocumento.APROBADO);
        docDni.setFechaEvaluacion(LocalDate.of(2026, 3, 9));
        docDni.setComentarioEvaluacion("Documento corregido y aceptado");

        // datos finales
        AntecedenteAcademico antColegio = new AntecedenteAcademico(postulante, colegio, 2021, 2025, "Secundaria completa");
        List<AntecedenteAcademico> ants = new ArrayList<>();
        ants.add(antColegio);
        postulante.setAntecedentes(ants);
        List<Postulacion> postulaciones = new ArrayList<>();
        postulaciones.add(postulacion);
        postulante.setPostulaciones(postulaciones);
        List<Postulante> postulantesConv = new ArrayList<>();
        postulantesConv.add(postulante);
        convocatoria.setPostulantes(postulantesConv);

        // baja logica
        contabilidad.setActivo(false);

        // mostrar por consola
        System.out.println("=== FLUJO ADMITU ===");
        System.out.println("pe.edu.pucp.admitu.persona.Postulante: " + postulante.getNombres() + " " + postulante.getApellidoPaterno());
        System.out.println("pe.edu.pucp.admitu.configuracion.Convocatoria: " + convocatoria.getNombre() + " (" + convocatoria.getPeriodo() + ") activo=" + convocatoria.isActivo());
        System.out.println("pe.edu.pucp.admitu.configuracion.Modalidad: " + postulacion.getModalidadElegida().getModalidad().getNombre());
        System.out.println("pe.edu.pucp.admitu.configuracion.Carrera: " + postulacion.getCarreraElegida().getCarrera().getNombre());
        System.out.println("Estado: " + postulacion.getEstadoActual().getNombre());
        System.out.println("Inscripcion: " + postulacion.getCodigoInscripcion());
        System.out.println("pe.edu.pucp.admitu.pago.Pago: S/ " + pago.getMonto() + " [" + pago.getEstadoPago() + "] ref=" + pago.getReferenciaPasarela());
        System.out.println("Documentos: " + postulacion.getDocumentos().size() + " (DNI: " + docDni.getEstadoDocumento() + ")");
        System.out.println("Antecedentes: " + postulante.getAntecedentes().size() + " (" + antColegio.getInstitucion().getNombre() + ")");
        System.out.println("Modalidades habilitadas: " + convocatoria.getModalidadesHabilitadas().size() + " | Sedes: " + lima.getNombre() + ", " + arequipa.getNombre());
        System.out.println("Notificaciones: " + postulacion.getNotificaciones().size() + " (bandeja leida=" + notifBandeja.isLeida() + ")");
        System.out.println("Carne: " + carne.getCodigoCarne() + " en " + carne.getSede().getNombre() + " (" + carne.getAulaExamen() + ")");
        System.out.println("Baja logica Contabilidad activo=" + contabilidad.isActivo());
    }
}