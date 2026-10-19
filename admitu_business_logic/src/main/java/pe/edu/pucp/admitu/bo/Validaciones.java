package pe.edu.pucp.admitu.bo;

import pe.edu.pucp.admitu.persona.TipoDocumento;
import java.time.LocalDate;
import java.math.BigDecimal;

public final class Validaciones {
    private Validaciones() { }

    public static void texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException(campo + " es obligatorio");
        longitud(valor, campo, maximo);
    }

    public static void longitud(String valor, String campo, int maximo) {
        if (valor != null && valor.length() > maximo)
            throw new IllegalArgumentException(campo + " excede " + maximo + " caracteres");
    }

    public static void codigo(String valor, String campo, int maximo) {
        texto(valor, campo, maximo);
        if (!valor.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException(campo + " contiene caracteres inválidos");
    }

    public static void correo(String valor) {
        texto(valor, "Correo", 150);
        if (!valor.matches("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(?:\\.[A-Za-z0-9\\-]+)+$"))
            throw new IllegalArgumentException("Correo con formato inválido");
    }

    public static void documento(TipoDocumento tipo, String numero) {
        if (tipo == null) throw new IllegalArgumentException("Tipo de documento obligatorio");
        texto(numero, "Número de documento", 20);
        boolean valido = switch (tipo) {
            case DNI -> numero.matches("[0-9]{8}");
            case CE -> numero.matches("[A-Za-z0-9]{9,12}");
            case PAS -> numero.matches("[A-Za-z0-9]{6,12}");
            case CPP -> numero.matches("[A-Za-z0-9]{8,15}");
        };
        if (!valido) throw new IllegalArgumentException("Formato de documento no válido para " + tipo);
    }

    public static void telefono(String numero) {
        longitud(numero, "Teléfono", 20);
        if (numero != null && !numero.isBlank()
                && !numero.matches("\\+?[0-9][0-9 ]{5,18}[0-9]"))
            throw new IllegalArgumentException("Teléfono con formato inválido");
    }

    public static void persona(String nombres, String paterno, String materno,
                               String correo, TipoDocumento tipo, String documento, String telefono) {
        texto(nombres, "Nombres", 100);
        texto(paterno, "Apellido paterno", 100);
        texto(materno, "Apellido materno", 100);
        correo(correo);
        documento(tipo, documento);
        telefono(telefono);
    }

    public static void rango(LocalDate inicio, LocalDate fin, String campo, boolean obligatorio) {
        if (obligatorio && (inicio == null || fin == null))
            throw new IllegalArgumentException("Se requieren las fechas de " + campo);
        if (inicio != null && fin != null && inicio.isAfter(fin))
            throw new IllegalArgumentException("Fechas inconsistentes de " + campo);
    }

    public static void noFutura(LocalDate fecha, String campo) {
        if (fecha != null && fecha.isAfter(LocalDate.now()))
            throw new IllegalArgumentException(campo + " no puede ser futura");
    }

    public static void monto(double valor, String campo, boolean permitirCero) {
        if (!Double.isFinite(valor) || valor < 0 || (!permitirCero && valor == 0)
                || valor > 99999999.99 || BigDecimal.valueOf(valor).scale() > 2)
            throw new IllegalArgumentException(campo + " debe tener hasta dos decimales y un importe válido");
    }

    public static void positivo(int id, String campo) {
        if (id <= 0) throw new IllegalArgumentException(campo + " debe ser positivo");
    }
}
