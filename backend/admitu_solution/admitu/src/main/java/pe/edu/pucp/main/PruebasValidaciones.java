package pe.edu.pucp.main;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.EstadoConvocatoria;
import pe.edu.pucp.admitu.configuracion.Facultad;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.FacultadBOImpl;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.pago.MedioPago;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.persona.bo.PostulanteBOImpl;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.EstadoPostulacionBOImpl;

import java.time.LocalDate;

/**
 * Pruebas de las validaciones de la capa de logica de negocio.
 *
 * Bloque 1: reglas de {@link Validaciones} probadas por separado (sin base de datos).
 * Bloque 2: validaciones invocadas desde los BO. Todos los casos son RECHAZADOS,
 *           es decir, la validacion ocurre ANTES de llegar a la capa de acceso a datos
 *           y por eso no se necesita conexion para ejecutar esta clase.
 *
 * Se corre desde IntelliJ (clase PruebasValidaciones -> Run), no requiere base de datos.
 */
public class PruebasValidaciones {

    private static int pruebas = 0;

    public static void main(String[] args) {
        System.out.println("=== Bloque 1: reglas de Validaciones ===");
        bloqueReglas();

        System.out.println("\n=== Bloque 2: validaciones invocadas desde los BO ===");
        bloqueCapaNegocio();

        System.out.println("\nResultado: " + pruebas + " casos verificados, 0 fallos.");
    }

    // ------------------------------------------------------------------
    // Bloque 1: reglas basicas (texto, formato y rangos)
    // ------------------------------------------------------------------
    private static void bloqueReglas() {
        acepta("DNI de 8 digitos", () -> Validaciones.documento(TipoDocumento.DNI, "12345678"));
        rechazado("DNI de 7 digitos", () -> Validaciones.documento(TipoDocumento.DNI, "1234567"));
        rechazado("DNI con letras", () -> Validaciones.documento(TipoDocumento.DNI, "12ABC789"));
        rechazado("DNI de 9 digitos", () -> Validaciones.documento(TipoDocumento.DNI, "123456789"));
        rechazado("documento vacio", () -> Validaciones.documento(TipoDocumento.DNI, "   "));

        acepta("correo valido", () -> Validaciones.correo("postulante@pucp.edu.pe"));
        rechazado("correo sin arroba", () -> Validaciones.correo("persona.pucp.edu.pe"));
        rechazado("correo sin dominio", () -> Validaciones.correo("persona@"));

        acepta("telefono valido", () -> Validaciones.telefono("987654321"));
        rechazado("telefono con letras", () -> Validaciones.telefono("9ABC54321"));

        rechazado("texto obligatorio vacio", () -> Validaciones.texto("   ", "Nombre", 100));
        rechazado("texto que excede la longitud", () -> Validaciones.texto("a".repeat(21), "Codigo", 20));
        rechazado("codigo con espacios", () -> Validaciones.codigo("FAC 01", "Codigo", 20));

        acepta("monto con dos decimales", () -> Validaciones.monto(350.00, "Monto", true));
        rechazado("monto negativo", () -> Validaciones.monto(-1, "Monto", true));
        rechazado("monto con mas de dos decimales", () -> Validaciones.monto(1.2345, "Monto", true));

        acepta("rango de fechas coherente", () -> Validaciones.rango(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1), "convocatoria", false));
        rechazado("rango de fechas invertido", () -> Validaciones.rango(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 1, 1), "convocatoria", false));
        rechazado("fecha futura", () -> Validaciones.noFutura(LocalDate.now().plusDays(1), "Fecha de carga"));
        rechazado("id no positivo", () -> Validaciones.positivo(0, "Id de facultad"));
    }

    // ------------------------------------------------------------------
    // Bloque 2: los BO rechazan el dato ANTES de tocar la base de datos
    // ------------------------------------------------------------------
    private static void bloqueCapaNegocio() {
        PostulanteBOImpl postulanteBO = new PostulanteBOImpl();
        Postulante base = new Postulante("Juan", "Perez", "Garcia", "juan.perez@pucp.edu.pe",
                TipoDocumento.DNI, "12345678", "987654321",
                null, LocalDate.of(2005, 5, 10), false, null, false, null, null, null);

        Postulante dniCorto = postulante(base);
        dniCorto.setNumeroDocumento("12345");
        rechazado("PostulanteBO: DNI de 5 digitos", () -> postulanteBO.insertar(dniCorto));

        Postulante dniConLetras = postulante(base);
        dniConLetras.setNumeroDocumento("12ABC789");
        rechazado("PostulanteBO: DNI con letras", () -> postulanteBO.insertar(dniConLetras));

        Postulante correoMalo = postulante(base);
        correoMalo.setCorreo("no-es-un-correo");
        rechazado("PostulanteBO: correo invalido", () -> postulanteBO.insertar(correoMalo));

        Postulante telefonoMalo = postulante(base);
        telefonoMalo.setTelefono("llamar despues");
        rechazado("PostulanteBO: telefono con letras", () -> postulanteBO.insertar(telefonoMalo));

        Postulante nacimientoFuturo = postulante(base);
        nacimientoFuturo.setFechaNacimiento(LocalDate.now().plusYears(1));
        rechazado("PostulanteBO: fecha de nacimiento futura",
                () -> postulanteBO.insertar(nacimientoFuturo));

        FacultadBOImpl facultadBO = new FacultadBOImpl();
        rechazado("FacultadBO: codigo con caracteres invalidos",
                () -> facultadBO.insertar(new Facultad("FAC 01!", "Facultad de prueba")));
        rechazado("FacultadBO: codigo que excede 20 caracteres",
                () -> facultadBO.insertar(new Facultad("F".repeat(21), "Facultad de prueba")));
        rechazado("FacultadBO: nombre obligatorio",
                () -> facultadBO.insertar(new Facultad("FAC01", "   ")));

        EstadoPostulacionBOImpl estadoBO = new EstadoPostulacionBOImpl();
        rechazado("EstadoPostulacionBO: codigo obligatorio",
                () -> estadoBO.insertar(new EstadoPostulacion("", "Pendiente", "Estado de prueba")));

        ConvocatoriaBOImpl convocatoriaBO = new ConvocatoriaBOImpl();
        Convocatoria invertida = new Convocatoria();
        invertida.setCodigoConvocatoria("CV-TEST");
        invertida.setNombre("Convocatoria de prueba");
        invertida.setPeriodo("2026-2");
        invertida.setEstado(EstadoConvocatoria.PUBLICADA);
        invertida.setFechaInicio(LocalDate.of(2026, 11, 1));
        invertida.setFechaFin(LocalDate.of(2026, 10, 1));
        rechazado("ConvocatoriaBO: fecha de inicio posterior a la de fin",
                () -> convocatoriaBO.insertar(invertida));

        PagoBOImpl pagoBO = new PagoBOImpl();
        Postulacion postulacion = new Postulacion();
        postulacion.setId(1);
        rechazado("PagoBO: monto negativo", () -> pagoBO.insertar(new Pago(postulacion,
                MedioPago.TARJETA, -1, LocalDate.now(), null,
                "PAG-TEST", "REF-TEST", EstadoPago.PENDIENTE, null)));
        rechazado("PagoBO: codigo de pago con espacios", () -> pagoBO.insertar(new Pago(postulacion,
                MedioPago.TARJETA, 350.00, LocalDate.now(), null,
                "PAG TEST", "REF-TEST", EstadoPago.PENDIENTE, null)));
        rechazado("PagoBO: transferencia sin voucher", () -> pagoBO.insertar(new Pago(postulacion,
                MedioPago.TRANSFERENCIA, 350.00, LocalDate.now(), null,
                "PAG-TEST", "REF-TEST", EstadoPago.PENDIENTE, null)));
    }

    // ------------------------------------------------------------------
    // utileria de prueba
    // ------------------------------------------------------------------
    private static Postulante postulante(Postulante modelo) {
        return new Postulante(modelo.getNombres(), modelo.getApellidoPaterno(), modelo.getApellidoMaterno(),
                modelo.getCorreo(), modelo.getTipoDocumento(), modelo.getNumeroDocumento(),
                modelo.getTelefono(), null, modelo.getFechaNacimiento(), false, null, false, null,
                null, null);
    }

    /** El caso debe terminar con IllegalArgumentException. */
    private static void rechazado(String caso, Runnable accion) {
        try {
            accion.run();
        } catch (IllegalArgumentException esperado) {
            pruebas++;
            System.out.println("  OK (rechazado) " + caso + " -> " + esperado.getMessage());
            return;
        }
        throw new IllegalStateException("NO fue rechazado: " + caso);
    }

    /** El caso no debe lanzar ninguna excepcion. */
    private static void acepta(String caso, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Fue rechazado [" + caso + "]: " + ex.getMessage());
        }
        pruebas++;
        System.out.println("  OK (aceptado)  " + caso);
    }
}
