import java.sql.SQLException;
import pe.edu.pucp.admitu.config.Diagnostico;

/** Verifica redacción de datos sensibles usando la implementación de producción. */
public final class PruebaDiagnostico {
    public static void main(String[] args) {
        String privado = "correo-privado@example.test cookie=SECRETO token=SECRETO SELECT clave";
        Throwable[] errores = {
            new SQLException(privado, "23000", 1062),
            new IllegalStateException(privado, new SQLException(privado, "45000", 1644)),
            new RuntimeException(privado),
            new SQLException(privado, privado, 0),
            null
        };
        for (int i = 0; i < errores.length; i++) {
            String resumen = Diagnostico.resumen(errores[i]);
            if (resumen.contains("SECRETO") || resumen.contains("example.test") || resumen.contains("SELECT"))
                throw new AssertionError("Diagnóstico contiene información privada");
            if (i == 0 && (!resumen.contains("23000") || !resumen.contains("1062")))
                throw new AssertionError("Diagnóstico perdió SQLState/código");
            if (i == 1 && (!resumen.contains("45000") || !resumen.contains("1644")))
                throw new AssertionError("Diagnóstico perdió causa SQL anidada");
            System.out.println("PASS diagnóstico sanitizado " + (i + 1));
        }
        var uno = new RuntimeException(privado);
        var dos = new RuntimeException(privado, uno);
        uno.initCause(dos);
        if (!Diagnostico.resumen(uno).equals("tipo=RuntimeException"))
            throw new AssertionError("Diagnóstico no controla cadena cíclica");
        System.out.println("PASS diagnóstico con causas cíclicas");
    }
}
