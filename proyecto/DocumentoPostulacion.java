import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class DocumentoPostulacion {
    private static int siguienteId = 1;
    private final int id;
    private Postulacion postulacion;
    private Requisito requisitoAplicable;
    private int numeroVersion;
    private String nombreArchivo;
    private TipoArchivo tipoArchivo;
    private long tamanioArchivo;
    private String rutaArchivo;
    private LocalDate fechaCarga;
    private EstadoDocumento estadoDocumento;
    private LocalDate fechaEvaluacion;
    private String comentarioEvaluacion;
    private List<DocumentoObservacion> observaciones;

    public DocumentoPostulacion(Postulacion postulacion, Requisito requisitoAplicable, int numeroVersion
            , String nombreArchivo, TipoArchivo tipoArchivo, long tamanioArchivo, String rutaArchivo
            , LocalDate fechaCarga, EstadoDocumento estadoDocumento, LocalDate fechaEvaluacion
            , String comentarioEvaluacion, List<DocumentoObservacion> observaciones) {
        this.id = siguienteId++;
        this.postulacion = postulacion;
        this.requisitoAplicable = requisitoAplicable;
        this.numeroVersion = numeroVersion;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.tamanioArchivo = tamanioArchivo;
        this.rutaArchivo = rutaArchivo;
        this.fechaCarga = fechaCarga;
        this.estadoDocumento = estadoDocumento;
        this.fechaEvaluacion = fechaEvaluacion;
        this.comentarioEvaluacion = comentarioEvaluacion;
        this.observaciones = new ArrayList<>(observaciones);
    }

    public int getId() {
        return id;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public Requisito getRequisitoAplicable() {
        return requisitoAplicable;
    }

    public void setRequisitoAplicable(Requisito requisitoAplicable) {
        this.requisitoAplicable = requisitoAplicable;
    }

    public int getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(int numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public TipoArchivo getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(TipoArchivo tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public long getTamanioArchivo() {
        return tamanioArchivo;
    }

    public void setTamanioArchivo(long tamanioArchivo) {
        this.tamanioArchivo = tamanioArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public LocalDate getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDate fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public EstadoDocumento getEstadoDocumento() {
        return estadoDocumento;
    }

    public void setEstadoDocumento(EstadoDocumento estadoDocumento) {
        this.estadoDocumento = estadoDocumento;
    }

    public LocalDate getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDate fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public String getComentarioEvaluacion() {
        return comentarioEvaluacion;
    }

    public void setComentarioEvaluacion(String comentarioEvaluacion) {
        this.comentarioEvaluacion = comentarioEvaluacion;
    }

    public List<DocumentoObservacion> getObservaciones() {
        return new ArrayList<DocumentoObservacion>(observaciones);
    }

    public void setObservaciones(List<DocumentoObservacion> observaciones) {
        this.observaciones = observaciones;
    }
}
