package pe.edu.pucp.main;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.configuracion.bo.*;
import pe.edu.pucp.admitu.pago.*;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.persona.*;
import pe.edu.pucp.admitu.persona.bo.PostulanteBOImpl;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.bo.*;

import java.time.LocalDate;

public class Principal {
    public static void main(String[] args) {
        System.out.println("=== AdmitU: prueba de integración del backend ===");
        System.out.println("Usar una base de datos de PRUEBA creada con los dos scripts SQL.\n");

        String sufijo = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        int numeroDni = 10_000_000 + (int) ((System.nanoTime() & Long.MAX_VALUE) % 90_000_000);

        FacultadBOImpl facultades = new FacultadBOImpl();
        CarreraBOImpl carreras = new CarreraBOImpl();
        ConvocatoriaBOImpl convocatorias = new ConvocatoriaBOImpl();
        PostulanteBOImpl postulantes = new PostulanteBOImpl();
        ModalidadBOImpl modalidades = new ModalidadBOImpl();
        OfertaCarreraBOImpl ofertas = new OfertaCarreraBOImpl();
        ConvocatoriaModalidadBOImpl convocatoriaModalidades = new ConvocatoriaModalidadBOImpl();
        EstadoPostulacionBOImpl estados = new EstadoPostulacionBOImpl();
        PostulacionBOImpl postulaciones = new PostulacionBOImpl();
        PagoBOImpl pagos = new PagoBOImpl();

        try {
            System.out.println("1) FACULTAD: insertar, obtener, modificar, listar y eliminar");
            Facultad facultad = new Facultad("F" + sufijo, "Facultad de prueba");
            facultades.insertar(facultad);
            comprobar(facultades.buscarPorId(facultad.getId()) != null, "No se insertó facultad");
            facultad.setNombre("Facultad actualizada");
            facultades.modificar(facultad);
            comprobar("Facultad actualizada".equals(facultades.buscarPorId(facultad.getId()).getNombre()),
                    "Falló la modificación de facultad");
            System.out.println("   Id=" + facultad.getId() + ", filas listadas=" + facultades.listarTodos().size());

            System.out.println("2) CARRERA: asociar a facultad recién creada");
            Carrera carrera = new Carrera(facultad, "C" + sufijo, "Carrera de prueba");
            carreras.insertar(carrera);
            comprobar(carreras.buscarPorId(carrera.getId()) != null, "No se insertó carrera");
            carrera.setNombre("Carrera actualizada");
            carreras.modificar(carrera);
            comprobar("Carrera actualizada".equals(carreras.buscarPorId(carrera.getId()).getNombre()),
                    "Falló la modificación de carrera");
            System.out.println("   Id=" + carrera.getId() + ", filas listadas=" + carreras.listarTodos().size());

            System.out.println("3) CONVOCATORIA: fechas y estado consistentes");
            Convocatoria convocatoria = new Convocatoria();
            convocatoria.setCodigoConvocatoria("V" + sufijo);
            convocatoria.setNombre("Convocatoria de laboratorio");
            convocatoria.setPeriodo("2026-2");
            convocatoria.setFechaInicio(LocalDate.of(2026, 10, 1));
            convocatoria.setFechaFin(LocalDate.of(2026, 11, 30));
            convocatoria.setEstado(EstadoConvocatoria.PUBLICADA);
            convocatoria.setDescripcion("Prueba de persistencia");
            convocatorias.insertar(convocatoria);
            convocatoria.setDescripcion("Convocatoria actualizada");
            convocatorias.modificar(convocatoria);
            comprobar("Convocatoria actualizada".equals(convocatorias.buscarPorId(convocatoria.getId()).getDescripcion()),
                    "Falló modificación de convocatoria");
            System.out.println("   Id=" + convocatoria.getId() + ", filas listadas=" + convocatorias.listarTodos().size());

            System.out.println("4) POSTULANTE: validar identidad y datos de contacto");
            Postulante postulante = new Postulante("Ana María", "Torres", "Flores",
                    "admision." + sufijo.toLowerCase() + "@example.org", TipoDocumento.DNI,
                    String.valueOf(numeroDni), "987654321", null, LocalDate.of(2005, 5, 10),
                    false, null, false, null, null, null);
            postulantes.insertar(postulante);
            postulante.setTelefono("988123456");
            postulantes.modificar(postulante);
            comprobar("988123456".equals(postulantes.buscarPorId(postulante.getId()).getTelefono()),
                    "Falló modificación de postulante");
            System.out.println("   Id=" + postulante.getId() + ", filas listadas=" + postulantes.listarTodos().size());

            System.out.println("5) VALIDACIÓN NEGATIVA: el DNI de siete cifras debe fallar");
            try {
                Validaciones.documento(TipoDocumento.DNI, "1234567");
                throw new IllegalStateException("ERROR: se aceptó un DNI de siete cifras");
            } catch (IllegalArgumentException esperado) {
                System.out.println("   Correctamente rechazado: " + esperado.getMessage());
            }

            int[] idPagoDePrueba = {0};
            try {
                TransactionContext.ejecutar(() -> {
            System.out.println("6) REGISTRO DE POSTULACIÓN Y PAGO EXTERNO");
            Modalidad modalidad = new Modalidad("M" + sufijo, "Modalidad de prueba",
                    "Admisión de prueba", true, false);
            modalidades.insertar(modalidad);
            ConvocatoriaModalidad cm = new ConvocatoriaModalidad(convocatoria, modalidad, 350.00, null, null);
            convocatoriaModalidades.insertar(cm);
            OfertaCarrera oferta = new OfertaCarrera(convocatoria, carrera, 20);
            ofertas.insertar(oferta);
            EstadoPostulacion estado = new EstadoPostulacion("E" + sufijo, "Registrado", "Registro inicial");
            estados.insertar(estado);

            Postulacion postulacion = new Postulacion();
            postulacion.setPostulante(postulante);
            postulacion.setConvocatoria(convocatoria);
            postulacion.setModalidadElegida(cm);
            postulacion.setCarreraElegida(oferta);
            postulacion.setEstadoActual(estado);
            postulacion.setCodigoInscripcion("I" + sufijo);

            Pago pago = new Pago(postulacion, MedioPago.TRANSFERENCIA, 350.00,
                    LocalDate.now(), null, "P" + sufijo, "TRX" + sufijo,
                    EstadoPago.PENDIENTE, "/vouchers/" + sufijo + ".pdf");
            new RegistroAdmisionBO().registrar(postulacion, pago);
            comprobar(pagos.buscarPorId(pago.getId()).getFechaPago() == null,
                    "Un pago PENDIENTE no debe tener fecha de pago");
            pago.setEstadoPago(EstadoPago.APROBADO);
            pagos.modificar(pago);
            Pago aprobado = pagos.buscarPorId(pago.getId());
            comprobar(aprobado.getEstadoPago() == EstadoPago.APROBADO && aprobado.getFechaPago() != null,
                    "Se aprobó sin asignar fecha_pago");
            System.out.println("   Pago " + aprobado.getId() + ": " + aprobado.getEstadoPago()
                    + " / fecha_pago=" + aprobado.getFechaPago());

                    idPagoDePrueba[0] = pago.getId();
                    throw new IllegalArgumentException("Finalización intencional de la prueba de pago");
                });
            } catch (IllegalArgumentException esperado) {
                comprobar(idPagoDePrueba[0] > 0, "El escenario de pago no llegó a ejecutarse");
                comprobar(pagos.buscarPorId(idPagoDePrueba[0]) == null,
                        "El pago de prueba quedó guardado después del rollback");
                System.out.println("   Pago y relaciones temporales revertidos correctamente.");
            }

            System.out.println("7) ROLLBACK: el error de una operación compuesta no debe dejar datos");
            String codigoFallido = "X" + sufijo;
            try {
                TransactionContext.ejecutar(() -> {
                    Facultad temporal = new Facultad(codigoFallido, "No debe persistir");
                    facultades.insertar(temporal);
                    throw new IllegalArgumentException("Error provocado para comprobar rollback");
                });
                throw new IllegalStateException("ERROR: la transacción debió fallar");
            } catch (IllegalArgumentException esperado) {
                boolean existe = facultades.listarTodos().stream()
                        .anyMatch(f -> codigoFallido.equals(f.getCodigo()));
                comprobar(!existe, "ROLLBACK falló: quedó la facultad temporal");
                System.out.println("   Rollback comprobado: no se guardó el registro temporal");
            }

            System.out.println("8) ELIMINAR las cuatro entidades: probar resultado de baja");
            postulantes.eliminar(postulante.getId());
            comprobar(postulantes.buscarPorId(postulante.getId()) == null, "No se eliminó el postulante");
            convocatorias.eliminar(convocatoria.getId());
            comprobar(!convocatorias.buscarPorId(convocatoria.getId()).isActivo(), "No se inactivó convocatoria");
            carreras.eliminar(carrera.getId());
            comprobar(!carreras.buscarPorId(carrera.getId()).isActivo(), "No se inactivó carrera");
            facultades.eliminar(facultad.getId());
            comprobar(!facultades.buscarPorId(facultad.getId()).isActivo(), "No se inactivó facultad");
            System.out.println("   Bajas verificadas en BD; los catálogos mantienen su historial.");
            System.out.println("\nRESULTADO: pruebas de laboratorio completadas.");
        } catch (RuntimeException ex) {
            System.err.println("PRUEBA DETENIDA: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalStateException(mensaje);
    }
}
