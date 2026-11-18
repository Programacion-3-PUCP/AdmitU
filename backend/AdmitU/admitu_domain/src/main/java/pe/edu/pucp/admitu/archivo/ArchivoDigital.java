package pe.edu.pucp.admitu.archivo;

import java.time.LocalDate;

/**
 * Entidad de dominio para los archivos binarios persistidos en archivo_digital
 * (DNI, fotografía, certificados académicos, vouchers de pago y documentos de postulación).
 */
public class ArchivoDigital {

    private int id;
    private int postulanteId;
    private int referenciaId;
    private int requisitoAplicableId;
    private String tipo;
    private String nombre;
    private String mime;
    private String contenidoBase64;
    private String sha256;
    private long tamanio;
    private LocalDate fechaRegistro;
    private String tipoInstitucion = "COLEGIO";
    private String institucion;
    private int anioInicio;
    private int anioFin;
    private String codigoOperacion;
    private double monto;

    public ArchivoDigital() {
        this.tipoInstitucion = "COLEGIO";
    }

    public ArchivoDigital(int postulanteId, String tipo, String nombre, String mime,
                          String contenidoBase64, LocalDate fechaRegistro) {
        this.postulanteId = postulanteId;
        this.tipo = tipo;
        this.nombre = nombre;
        this.mime = mime;
        this.contenidoBase64 = contenidoBase64;
        this.fechaRegistro = fechaRegistro;
        this.tipoInstitucion = "COLEGIO";
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        this.id = value;
    }

    public int getPostulanteId() {
        return postulanteId;
    }

    public void setPostulanteId(int value) {
        this.postulanteId = value;
    }

    public int getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(int value) {
        this.referenciaId = value;
    }

    public int getRequisitoAplicableId() {
        return requisitoAplicableId;
    }

    public void setRequisitoAplicableId(int value) {
        this.requisitoAplicableId = value;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String value) {
        this.tipo = value;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String value) {
        this.nombre = value;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String value) {
        this.mime = value;
    }

    public String getContenidoBase64() {
        return contenidoBase64;
    }

    public void setContenidoBase64(String value) {
        this.contenidoBase64 = value;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String value) {
        this.sha256 = value;
    }

    public long getTamanio() {
        return tamanio;
    }

    public void setTamanio(long value) {
        this.tamanio = value;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate value) {
        this.fechaRegistro = value;
    }

    public String getTipoInstitucion() {
        return tipoInstitucion;
    }

    public void setTipoInstitucion(String tipo) {
        this.tipoInstitucion = tipo;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String value) {
        this.institucion = value;
    }

    public int getAnioInicio() {
        return anioInicio;
    }

    public void setAnioInicio(int value) {
        this.anioInicio = value;
    }

    public int getAnioFin() {
        return anioFin;
    }

    public void setAnioFin(int value) {
        this.anioFin = value;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String value) {
        this.codigoOperacion = value;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double value) {
        this.monto = value;
    }
}
