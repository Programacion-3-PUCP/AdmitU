package pe.edu.pucp.main;

import pe.edu.pucp.admitu.configuracion.bo.CarreraBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.FacultadBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.ICarreraBO;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaBO;
import pe.edu.pucp.admitu.configuracion.boi.IFacultadBO;
import pe.edu.pucp.admitu.configuracion.Carrera;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.EstadoConvocatoria;
import pe.edu.pucp.admitu.configuracion.Facultad;
import pe.edu.pucp.admitu.configuracion.OfertaCarrera;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.pago.MedioPago;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.pago.boi.IPagoBO;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.persona.bo.PostulanteBOImpl;
import pe.edu.pucp.admitu.persona.boi.IPostulanteBO;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;

import java.time.LocalDate;

public class Principal {
    public static void main(String[] args) {
        // suf para no chocar con los unique cuando se corre varias veces
        String suf = String.valueOf(System.currentTimeMillis() % 100000);
        String dni = "9" + String.format("%07d", System.currentTimeMillis() % 10000000);
        System.out.println("AdmitU");

        IFacultadBO facultadBO = new FacultadBOImpl();
        ICarreraBO carreraBO = new CarreraBOImpl();
        IConvocatoriaBO convocatoriaBO = new ConvocatoriaBOImpl();
        IPostulanteBO postulanteBO = new PostulanteBOImpl();
        IPostulacionBO postulacionBO = new PostulacionBOImpl();
        IPagoBO pagoBO = new PagoBOImpl();

        try {
            // facultad
            Facultad fac = new Facultad("FAC-" + suf, "Facultad " + suf);
            facultadBO.insertar(fac);
            System.out.println("Facultad registrada: " + fac.getId());
            System.out.println("Total facultades: " + facultadBO.listarTodos().size());
            System.out.println("Buscar facultad: " + facultadBO.buscarPorId(fac.getId()).getNombre());

            fac.setNombre("Facultad actualizada " + suf);
            facultadBO.modificar(fac);
            System.out.println("Facultad modificada: " + facultadBO.buscarPorId(fac.getId()).getNombre());

            facultadBO.eliminar(fac.getId());
            System.out.println("Facultad eliminada, activo=" + facultadBO.buscarPorId(fac.getId()).isActivo());

            // carrera, uso la facultad 1 que ya existe en la bd
            Carrera car = new Carrera(facultadBO.buscarPorId(1), "CAR-" + suf, "Carrera " + suf);
            carreraBO.insertar(car);
            System.out.println("Carrera registrada: " + car.getId());

            car.setNombre("Carrera actualizada " + suf);
            carreraBO.modificar(car);
            System.out.println("Carrera modificada: " + carreraBO.buscarPorId(car.getId()).getNombre());
            System.out.println("Total carreras: " + carreraBO.listarTodos().size());

            carreraBO.eliminar(car.getId());
            System.out.println("Carrera eliminada: " + car.getId());

            // convocatoria
            Convocatoria conv = new Convocatoria();
            conv.setCodigoConvocatoria("CV-" + suf);
            conv.setNombre("Convocatoria " + suf);
            conv.setPeriodo("2026-2");
            conv.setFechaInicio(LocalDate.of(2026, 10, 1));
            conv.setFechaFin(LocalDate.of(2026, 11, 30));
            conv.setEstado(EstadoConvocatoria.PUBLICADA);
            conv.setDescripcion("Convocatoria de prueba");
            convocatoriaBO.insertar(conv);
            System.out.println("Convocatoria registrada: " + conv.getId());

            conv.setDescripcion("Convocatoria actualizada");
            convocatoriaBO.modificar(conv);
            System.out.println("Convocatoria modificada: " + convocatoriaBO.buscarPorId(conv.getId()).getDescripcion());
            System.out.println("Total convocatorias: " + convocatoriaBO.listarTodos().size());

            // postulante
            Postulante post = new Postulante("Postulante", "Paterno" + suf, "Materno", "postulante." + suf + "@pucp.edu.pe",
                TipoDocumento.DNI, dni, "9" + dni,
                null, LocalDate.of(2005, 5, 10), false, null, false, null, null, null);
            postulanteBO.insertar(post);
            System.out.println("Postulante registrado: " + post.getId());

            post.setTelefono("988000111");
            postulanteBO.modificar(post);
            System.out.println("Telefono nuevo: " + postulanteBO.buscarPorId(post.getId()).getTelefono());

            // postulacion, se usan los ids 1 que ya estan en la bd
            ConvocatoriaModalidad modRef = new ConvocatoriaModalidad();
            modRef.setId(1);
            OfertaCarrera ofeRef = new OfertaCarrera();
            ofeRef.setId(1);
            EstadoPostulacion estRef = new EstadoPostulacion("TMP", "TMP", null);
            estRef.setId(1);

            Postulacion postulacion = new Postulacion();
            postulacion.setPostulante(post);
            postulacion.setConvocatoria(conv);
            postulacion.setModalidadElegida(modRef);
            postulacion.setCarreraElegida(ofeRef);
            postulacion.setEstadoActual(estRef);
            postulacion.setCodigoInscripcion("INS-" + suf);
            postulacion.setObservacionGeneral("Postulacion de prueba");
            postulacionBO.insertar(postulacion);
            System.out.println("Postulacion registrada: " + postulacion.getId());

            EstadoPostulacion est2 = new EstadoPostulacion("TMP", "TMP", null);
            est2.setId(2);
            postulacion.setEstadoActual(est2);
            postulacion.setObservacionGeneral("Postulacion actualizada");
            postulacionBO.modificar(postulacion);
            System.out.println("Postulacion modificada: " + postulacionBO.buscarPorId(postulacion.getId()).getObservacionGeneral());

            // pago por transferencia con voucher
            Pago pago = new Pago(postulacion, MedioPago.TRANSFERENCIA, 350.00,
                LocalDate.now(), null, "PAG-" + suf, "REF-" + suf,
                EstadoPago.PENDIENTE, "/vouchers/" + suf + ".pdf");
            pagoBO.insertar(pago);
            System.out.println("Pago registrado: " + pago.getId() + " " + pago.getMedioPago());

            pago.setEstadoPago(EstadoPago.APROBADO);
            pago.setReferenciaPasarela("REF-APROB-" + suf);
            pagoBO.modificar(pago);
            System.out.println("Pago aprobado: " + pagoBO.buscarPorId(pago.getId()).getEstadoPago());
            System.out.println("Total pagos: " + pagoBO.listarTodos().size());

            pagoBO.eliminar(pago.getId());
            System.out.println("Pago eliminado: " + pagoBO.buscarPorId(pago.getId()).getEstadoPago());

            // limpieza de lo que se registro en la demo
            postulacionBO.eliminar(postulacion.getId());
            postulanteBO.eliminar(post.getId());
            convocatoriaBO.eliminar(conv.getId());

            System.out.println("Demo terminada");
        } catch (Exception ex) {
            System.out.println("Error en la demo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
