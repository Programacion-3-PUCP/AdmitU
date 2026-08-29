import java.util.Date;

public class ProgramaPrincipal {
    public static void main(String[] args) {
        Date hoy = new Date();

        EstadoPostulacion borrador = new EstadoPostulacion("BORRADOR", "Borrador", "Postulación creada");
        EstadoPostulacion finalizada = new EstadoPostulacion("FINALIZADA", "Finalizada", "Inscripción completada");
        MedioPago transferencia = new MedioPago("TRANSFERENCIA", "Transferencia bancaria");

        Convocatoria conv = new Convocatoria("2026-2", hoy, hoy);
        conv.setEstado("P");

        Postulante postulante = new Postulante("Diego", "Osorio", "78965412", "diego@pucp.edu.pe");
        postulante.setCorreoValidado("N");

        Postulacion postulacion = new Postulacion(postulante, conv, null, null, borrador);

        Pago pago = new Pago(350.00, hoy, new MedioPago("TRANSFERENCIA", "Transferencia"));
        pago.setNumeroIntento(1);
        pago.setEstadoPago("A");

        postulante.setCorreoValidado("S");
        postulante.setFechaValidacionCorreo(new Date());

        postulante.mostrarInformacion();
        System.out.println("Estado postulación: " + postulacion.getEstadoActual().getCodigo());
        System.out.println("Pago estado: " + pago.getEstadoPago());
    }
}