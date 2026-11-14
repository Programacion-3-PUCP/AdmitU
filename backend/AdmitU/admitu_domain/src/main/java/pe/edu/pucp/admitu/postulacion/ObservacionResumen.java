package pe.edu.pucp.admitu.postulacion;

import java.time.LocalDate;

/** Metadatos globales de una observación; omite autor completo, archivos y descripción. */
public final class ObservacionResumen {
    private final int id, postulacionId, requisitoAplicableId, evaluadorId;
    private final TipoObservacion tipoObservacion;
    private final EstadoObservacion estadoObservacion;
    private final LocalDate fechaObservacion, fechaSubsanacion;

    public ObservacionResumen(DocumentoObservacion o, DocumentoPostulacion d) {
        id = o.getId(); postulacionId = d.getPostulacion().getId();
        requisitoAplicableId = d.getRequisitoAplicable().getId(); evaluadorId = o.getEvaluador().getId();
        tipoObservacion = o.getTipoObservacion(); estadoObservacion = o.getEstadoObservacion();
        fechaObservacion = o.getFechaObservacion(); fechaSubsanacion = o.getFechaSubsanacion();
    }
    public int getId() { return id; }
    public int getPostulacionId() { return postulacionId; }
    public int getRequisitoAplicableId() { return requisitoAplicableId; }
    public int getEvaluadorId() { return evaluadorId; }
    public TipoObservacion getTipoObservacion() { return tipoObservacion; }
    public EstadoObservacion getEstadoObservacion() { return estadoObservacion; }
    public LocalDate getFechaObservacion() { return fechaObservacion; }
    public LocalDate getFechaSubsanacion() { return fechaSubsanacion; }
}
