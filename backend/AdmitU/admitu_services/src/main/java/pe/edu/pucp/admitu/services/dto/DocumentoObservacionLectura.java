package pe.edu.pucp.admitu.services.dto;

import java.time.LocalDate;
import pe.edu.pucp.admitu.postulacion.*;

/** Detalle autorizado sin los contactos o documento de identidad de su autor. */
public final class DocumentoObservacionLectura {
    public static final class ReferenciaId {
        private final int id;
        public ReferenciaId(int id) { this.id = id; }
        public int getId() { return id; }
    }
    private final DocumentoObservacion original;
    public DocumentoObservacionLectura(DocumentoObservacion original) { this.original = original; }
    public int getId() { return original.getId(); }
    public DocumentoPostulacion getDocumento() { return original.getDocumento(); }
    public ReferenciaId getEvaluador() { return original.getEvaluador() == null ? null : new ReferenciaId(original.getEvaluador().getId()); }
    public TipoObservacion getTipoObservacion() { return original.getTipoObservacion(); }
    public String getDescripcion() { return original.getDescripcion(); }
    public LocalDate getFechaObservacion() { return original.getFechaObservacion(); }
    public EstadoObservacion getEstadoObservacion() { return original.getEstadoObservacion(); }
    public LocalDate getFechaSubsanacion() { return original.getFechaSubsanacion(); }
    public String getComentarioSubsanacion() { return original.getComentarioSubsanacion(); }
}
