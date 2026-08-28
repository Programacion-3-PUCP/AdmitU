public class ProgramaPrincipal {
    public static void main(String[] args) {
        Date hoy = new Date();
        Convocatoria conv2026 = new Convocatoria("2026-2", hoy, hoy); 
        
        Postulante postulante1 = new Postulante("Diego", "Osorio", "78965412", EstadoPostulante.REGISTRADO);
        Pago pagoInscripcion = new Pago(350.00, hoy, MetodoPago.TRANSFERENCIA);
        
        postulante1.agregarPago(pagoInscripcion);
        postulante1.setEstado(EstadoPostulante.INSCRITO);
        
        conv2026.agregarPostulante(postulante1);
        
        System.out.println("--- Sistema AdmitU ---");
        System.out.println("Convocatoria: " + conv2026.getPeriodo());
        
        for (Postulante p : conv2026.getPostulantes()) {
            p.mostrarInformacion();
        }
    }
}
