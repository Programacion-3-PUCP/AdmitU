package pe.edu.pucp.admitu.postulacion;

import java.time.LocalDate;

public class ObservacionResumen {
    private int id;
    private int postulacionId;
    private int requisitoAplicableId;
    private int evaluadorId;
    private TipoObservacion tipoObservacion;
    private EstadoObservacion estadoObservacion;
    private LocalDate fechaObservacion;
    private LocalDate fechaSubsanacion;

    public ObservacionResumen() {
    }

    public ObservacionResumen(DocumentoObservacion o, DocumentoPostulacion d) {
        this.id = o.getId();
        this.postulacionId = d.getPostulacion().getId();
        this.requisitoAplicableId = d.getRequisitoAplicable().getId();
        this.evaluadorId = o.getEvaluador().getId();
        this.tipoObservacion = o.getTipoObservacion();
        this.estadoObservacion = o.getEstadoObservacion();
        this.fechaObservacion = o.getFechaObservacion();
        this.fechaSubsanacion = o.getFechaSubsanacion();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostulacionId() {
        return postulacionId;
    }

    public void setPostulacionId(int postulacionId) {
        this.postulacionId = postulacionId;
    }

    public int getRequisitoAplicableId() {
        return requisitoAplicableId;
    }

    public void setRequisitoAplicableId(int requisitoAplicableId) {
        this.requisitoAplicableId = requisitoAplicableId;
    }

    public int getEvaluadorId() {
        return evaluadorId;
    }

    public void setEvaluadorId(int evaluadorId) {
        this.evaluadorId = evaluadorId;
    }

    public TipoObservacion getTipoObservacion() {
        return tipoObservacion;
    }

    public void setTipoObservacion(TipoObservacion tipoObservacion) {
        this.tipoObservacion = tipoObservacion;
    }

    public EstadoObservacion getEstadoObservacion() {
        return estadoObservacion;
    }

    public void setEstadoObservacion(EstadoObservacion estadoObservacion) {
        this.estadoObservacion = estadoObservacion;
    }

    public LocalDate getFechaObservacion() {
        return fechaObservacion;
    }

    public void setFechaObservacion(LocalDate fechaObservacion) {
        this.fechaObservacion = fechaObservacion;
    }

    public LocalDate getFechaSubsanacion() {
        return fechaSubsanacion;
    }

    public void setFechaSubsanacion(LocalDate fechaSubsanacion) {
        this.fechaSubsanacion = fechaSubsanacion;
    }
}
