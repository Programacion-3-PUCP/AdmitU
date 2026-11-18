package pe.edu.pucp.admitu.archivo.bo;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.boi.IArchivoBO;
import pe.edu.pucp.admitu.archivo.dao.ArchivoDAO;
import pe.edu.pucp.admitu.archivo.impl.ArchivoImpl;
import pe.edu.pucp.admitu.config.TransactionContext;

public class ArchivoBOImpl implements IArchivoBO {

    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private ArchivoDAO daoArchivo;

    public ArchivoBOImpl() {
        daoArchivo = new ArchivoImpl();
    }

    private LocalDate hoy() {
        return LocalDate.now(ZoneId.of("America/Lima"));
    }

    private boolean tieneCaracterControl(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isISOControl(texto.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void validar(ArchivoDigital archivo) {
        if (archivo == null || archivo.getPostulanteId() <= 0) {
            throw new IllegalArgumentException("El archivo requiere un postulante válido");
        }
        String tipo = archivo.getTipo();
        boolean tipoValido = "DNI".equals(tipo) || "FOTOGRAFIA".equals(tipo)
                || "CERTIFICADO".equals(tipo) || "VOUCHER".equals(tipo) || "DOCUMENTO".equals(tipo);
        if (!tipoValido) {
            throw new IllegalArgumentException("Tipo de archivo inválido");
        }
        if (archivo.getNombre() == null
                || archivo.getNombre().trim().isEmpty()
                || archivo.getNombre().length() > 150
                || archivo.getNombre().contains("/")
                || archivo.getNombre().contains("\\")
                || tieneCaracterControl(archivo.getNombre())) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }
        if (archivo.getContenidoBase64() == null
                || archivo.getContenidoBase64().length() > ((MAX_BYTES + 2) / 3) * 4) {
            throw new IllegalArgumentException("Adjunta bytes reales, máximo 5 MB");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(archivo.getContenidoBase64());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Contenido base64 inválido");
        }

        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("Tamaño de archivo inválido, máximo 5 MB");
        }

        String nombreMin = archivo.getNombre().toLowerCase();
        boolean png = bytes.length >= 8
                && bytes[0] == (byte) 137
                && bytes[1] == 80
                && bytes[2] == 78
                && bytes[3] == 71
                && bytes[4] == 13
                && bytes[5] == 10
                && bytes[6] == 26
                && bytes[7] == 10;
        boolean jpg = bytes.length >= 4
                && bytes[0] == (byte) 255
                && bytes[1] == (byte) 216
                && bytes[2] == (byte) 255;
        boolean pdf = bytes.length >= 5
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-';

        boolean formatoValido = (png && nombreMin.endsWith(".png") && "image/png".equals(archivo.getMime()))
                || (jpg && (nombreMin.endsWith(".jpg") || nombreMin.endsWith(".jpeg")) && "image/jpeg".equals(archivo.getMime()))
                || (pdf && nombreMin.endsWith(".pdf") && "application/pdf".equals(archivo.getMime()));

        if (!formatoValido) {
            throw new IllegalArgumentException("Extensión, MIME y firma deben coincidir: JPG, PNG o PDF");
        }
        if (("DNI".equals(tipo) || "FOTOGRAFIA".equals(tipo)) && !(png || jpg)) {
            throw new IllegalArgumentException("DNI y fotografía solo admiten JPG/PNG");
        }
        if ("CERTIFICADO".equals(tipo) && !pdf) {
            throw new IllegalArgumentException("El certificado debe ser PDF");
        }
        if (archivo.getFechaRegistro() == null) {
            archivo.setFechaRegistro(hoy());
        }
        if (archivo.getFechaRegistro().isAfter(hoy()) || archivo.getFechaRegistro().isBefore(LocalDate.of(1900, 1, 1))) {
            throw new IllegalArgumentException("La fecha de registro no puede ser futura");
        }

        archivo.setTamanio(bytes.length);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            archivo.setSha256(hex.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ArchivoDigital guardarEnTransaccion(ArchivoDigital archivo) {
        validar(archivo);
        return daoArchivo.guardar(archivo);
    }

    @Override
    public ArchivoDigital guardar(ArchivoDigital archivo) {
        if (archivo == null || (!"DNI".equals(archivo.getTipo())
                && !"FOTOGRAFIA".equals(archivo.getTipo())
                && !"DOCUMENTO".equals(archivo.getTipo()))) {
            throw new IllegalArgumentException("Utiliza la operación correspondiente de certificado o voucher");
        }
        validar(archivo);
        try {
            ArchivoDigital resultado = daoArchivo.guardar(archivo);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error al guardar el archivo: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public ArchivoDigital guardarCertificado(ArchivoDigital archivo) {
        if (archivo == null || !"CERTIFICADO".equals(archivo.getTipo())) {
            throw new IllegalArgumentException("Certificado inválido");
        }
        validar(archivo);
        if (!"COLEGIO".equals(archivo.getTipoInstitucion()) && !"UNIVERSIDAD".equals(archivo.getTipoInstitucion())) {
            throw new IllegalArgumentException("Selecciona colegio o universidad");
        }
        if (archivo.getInstitucion() == null
                || archivo.getInstitucion().trim().isEmpty()
                || archivo.getInstitucion().length() > 150
                || archivo.getAnioInicio() < 1900
                || archivo.getAnioFin() < archivo.getAnioInicio()
                || archivo.getAnioFin() > hoy().getYear()) {
            throw new IllegalArgumentException("Institución o años de estudio inválidos");
        }
        try {
            daoArchivo.guardarCertificado(archivo);
            ArchivoDigital resultado = daoArchivo.guardar(archivo);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error al guardar el certificado: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public ArchivoDigital guardarVoucher(ArchivoDigital archivo) {
        if (archivo == null || !"VOUCHER".equals(archivo.getTipo())) {
            throw new IllegalArgumentException("Voucher inválido");
        }
        validar(archivo);
        if (archivo.getCodigoOperacion() == null
                || !archivo.getCodigoOperacion().matches("[A-Za-z0-9-]{4,30}")
                || !Double.isFinite(archivo.getMonto())
                || archivo.getMonto() <= 0) {
            throw new IllegalArgumentException("Número de operación o monto inválido");
        }
        try {
            daoArchivo.guardarVoucher(archivo);
            ArchivoDigital resultado = daoArchivo.guardar(archivo);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error al guardar el voucher: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public List<ArchivoDigital> listarPorPostulante(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Postulante inválido");
        }
        return daoArchivo.listarPorPostulante(id);
    }

    @Override
    public ArchivoDigital buscarPorId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Archivo inválido");
        }
        return daoArchivo.buscarPorId(id);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Archivo inválido");
        }
        try {
            int resultado = daoArchivo.eliminar(id);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error al eliminar el archivo: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }
}
