import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class DocumentoObservacion {
    private static int siguienteId = 1;
    private final int id;
    private DocumentoPostulacion documento;
    private Evaluador evaluador;
    private TipoObservacion tipoObservacion;
    private String descripcion;
    private LocalDate fechaObservacion;
    private EstadoObservacion estadoObservacion;
    private LocalDate fechaSubsanacion;
    private String comentarioSubsanacion;

    public DocumentoObservacion(DocumentoPostulacion documento, Evaluador evaluador
            , TipoObservacion tipoObservacion, String descripcion, LocalDate fechaObservacion
            , EstadoObservacion estadoObservacion, LocalDate fechaSubsanacion, String comentarioSubsanacion) {
        this.id = siguienteId++;
        this.documento = documento;
        this.evaluador = evaluador;
        this.tipoObservacion = tipoObservacion;
        this.descripcion = descripcion;
        this.fechaObservacion = fechaObservacion;
        this.estadoObservacion = estadoObservacion;
        this.fechaSubsanacion = fechaSubsanacion;
        this.comentarioSubsanacion = comentarioSubsanacion;
    }

    public int getId() {
        return id;
    }

    public DocumentoPostulacion getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoPostulacion documento) {
        this.documento = documento;
    }

    public Evaluador getEvaluador() {
        return evaluador;
    }

    public void setEvaluador(Evaluador evaluador) {
        this.evaluador = evaluador;
    }

    public TipoObservacion getTipoObservacion() {
        return tipoObservacion;
    }

    public void setTipoObservacion(TipoObservacion tipoObservacion) {
        this.tipoObservacion = tipoObservacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaObservacion() {
        return fechaObservacion;
    }

    public void setFechaObservacion(LocalDate fechaObservacion) {
        this.fechaObservacion = fechaObservacion;
    }

    public EstadoObservacion getEstadoObservacion() {
        return estadoObservacion;
    }

    public void setEstadoObservacion(EstadoObservacion estadoObservacion) {
        this.estadoObservacion = estadoObservacion;
    }

    public LocalDate getFechaSubsanacion() {
        return fechaSubsanacion;
    }

    public void setFechaSubsanacion(LocalDate fechaSubsanacion) {
        this.fechaSubsanacion = fechaSubsanacion;
    }

    public String getComentarioSubsanacion() {
        return comentarioSubsanacion;
    }

    public void setComentarioSubsanacion(String comentarioSubsanacion) {
        this.comentarioSubsanacion = comentarioSubsanacion;
    }
}
