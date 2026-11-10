package pe.edu.pucp.admitu.bo;

import pe.edu.pucp.admitu.persona.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reglas de validacion reutilizables por toda la capa de logica de negocio.
 *
 * Cada BO las invoca dentro de su metodo privado validar(), antes de llamar
 * a la capa de acceso a datos. La clase no tiene estado, por eso es final
 * y su constructor es privado.
 */
public final class Validaciones {

    private Validaciones() { }

    /** Obligatorio y hasta {@code maximo} caracteres. */
    public static void texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException(campo + " es obligatorio");
        longitud(valor, campo, maximo);
    }

    /** Solo longitud: el valor puede venir vacio o nulo. */
    public static void longitud(String valor, String campo, int maximo) {
        if (valor != null && valor.length() > maximo)
            throw new IllegalArgumentException(campo + " no debe exceder " + maximo + " caracteres");
    }

    /** Texto obligatorio, hasta {@code maximo} y solo letras, digitos, guion y guion bajo. */
    public static void codigo(String valor, String campo, int maximo) {
        texto(valor, campo, maximo);
        if (!valor.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException(campo + " contiene caracteres invalidos");
    }

    /** Correo obligatorio con formato valido (maximo 150 caracteres). */
    public static void correo(String valor) {
        texto(valor, "Correo", 150);
        if (!valor.matches("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(?:\\.[A-Za-z0-9\\-]+)+$"))
            throw new IllegalArgumentException("Correo con formato invalido");
    }

    /** Tipo de documento obligatorio y numero con el formato que le corresponde. */
    public static void documento(TipoDocumento tipo, String numero) {
        if (tipo == null) throw new IllegalArgumentException("Tipo de documento obligatorio");
        texto(numero, "Numero de documento", 20);
        boolean valido = switch (tipo) {
            case DNI -> numero.matches("[0-9]{8}");
            case CE -> numero.matches("[A-Za-z0-9]{9,12}");
            case PAS -> numero.matches("[A-Za-z0-9]{6,12}");
            case CPP -> numero.matches("[A-Za-z0-9]{8,15}");
        };
        if (!valido)
            throw new IllegalArgumentException("Numero de documento invalido para " + tipo
                    + ": se espera un valor de formato valido");
    }

    /** Telefono opcional: si viene, hasta 20 caracteres y solo digitos, espacios y + inicial. */
    public static void telefono(String numero) {
        longitud(numero, "Telefono", 20);
        if (numero != null && !numero.isBlank()
                && !numero.matches("\\+?[0-9][0-9 ]{5,18}[0-9]"))
            throw new IllegalArgumentException("Telefono con formato invalido");
    }

    /**
     * Datos de persona: nombres, apellido paterno, correo y documento son obligatorios;
     * el apellido materno y el telefono son opcionales (solo se controla su longitud).
     */
    public static void persona(String nombres, String paterno, String materno,
                               String correo, TipoDocumento tipo, String documento, String telefono) {
        texto(nombres, "Nombres", 100);
        texto(paterno, "Apellido paterno", 100);
        longitud(materno, "Apellido materno", 100);
        correo(correo);
        documento(tipo, documento);
        telefono(telefono);
    }

    /**
     * Rango de fechas coherente. Con {@code obligatorio} en true ninguna de las dos
     * puede faltar; con false solo se controla que inicio no sea posterior a fin.
     */
    public static void rango(LocalDate inicio, LocalDate fin, String campo, boolean obligatorio) {
        if (obligatorio && (inicio == null || fin == null))
            throw new IllegalArgumentException("Se requieren las fechas de " + campo);
        if (inicio != null && fin != null && inicio.isAfter(fin))
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la de " + campo);
    }

    /** La fecha no puede ser posterior a hoy (las nulas se ignoran). */
    public static void noFutura(LocalDate fecha, String campo) {
        if (fecha != null && fecha.isAfter(LocalDate.now()))
            throw new IllegalArgumentException(campo + " no puede ser futura");
    }

    /** Importe no negativo, con hasta dos decimales y hasta 99'999,999.99. */
    public static void monto(double valor, String campo, boolean permitirCero) {
        if (!Double.isFinite(valor) || valor < 0 || (!permitirCero && valor == 0)
                || valor > 99999999.99 || BigDecimal.valueOf(valor).scale() > 2)
            throw new IllegalArgumentException(campo + " debe ser un importe valido con hasta dos decimales");
    }

    /** Identificador numerico obligatorio (mayor que cero). */
    public static void positivo(int valor, String campo) {
        if (valor <= 0) throw new IllegalArgumentException(campo + " debe ser mayor que cero");
    }
}
