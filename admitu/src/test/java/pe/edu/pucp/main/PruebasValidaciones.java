package pe.edu.pucp.main;

import java.time.LocalDate;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.persona.TipoDocumento;

public class PruebasValidaciones {
    private static int pruebas = 0;

    public static void main(String[] args) {
        Validaciones.documento(TipoDocumento.DNI, "12345678");
        rechazado("DNI corto", () -> Validaciones.documento(TipoDocumento.DNI, "1234567"));
        rechazado("DNI con letras", () -> Validaciones.documento(TipoDocumento.DNI, "12345ABC"));
        rechazado("DNI largo", () -> Validaciones.documento(TipoDocumento.DNI, "123456789"));
        rechazado("Documento vacío", () -> Validaciones.documento(TipoDocumento.DNI, " "));
        Validaciones.documento(TipoDocumento.CE, "ABC123456");
        Validaciones.correo("postulante@example.org");
        rechazado("Correo inválido", () -> Validaciones.correo("persona@"));
        rechazado("Nombre ausente", () -> Validaciones.texto(" ", "Nombre", 100));
        Validaciones.telefono("987654321");
        rechazado("Teléfono con letras", () -> Validaciones.telefono("9ABC54321"));
        Validaciones.monto(350.00, "Pago", false);
        rechazado("Monto negativo", () -> Validaciones.monto(-1, "Pago", false));
        rechazado("Monto fraccionado inválido", () -> Validaciones.monto(1.2345, "Pago", false));
        rechazado("Monto infinito", () -> Validaciones.monto(Double.POSITIVE_INFINITY, "Pago", false));
        Validaciones.rango(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1), "convocatoria", true);
        rechazado("Fechas invertidas", () -> Validaciones.rango(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 1, 1), "convocatoria", true));
        rechazado("Fecha obligatoria", () -> Validaciones.rango(null, LocalDate.now(), "convocatoria", true));
        System.out.println("Resultado: " + pruebas + " casos inválidos rechazados; datos válidos aceptados.");
    }

    private static void rechazado(String caso, Runnable accion) {
        try {
            accion.run();
        } catch (IllegalArgumentException esperado) {
            pruebas++;
            System.out.println("OK: " + caso);
            return;
        }
        throw new IllegalStateException("Se aceptó un valor inválido: " + caso);
    }
}
