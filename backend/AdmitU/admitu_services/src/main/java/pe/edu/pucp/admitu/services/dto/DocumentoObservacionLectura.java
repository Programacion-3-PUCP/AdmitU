package pe.edu.pucp.admitu.services.dto;

import java.time.LocalDate;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.EstadoObservacion;
import pe.edu.pucp.admitu.postulacion.TipoObservacion;

public class DocumentoObservacionLectura {

    public static class ReferenciaId {
        private int id;

        public ReferenciaId() {
        }

        public ReferenciaId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    private DocumentoObservacion original;

    public DocumentoObservacionLectura() {
    }

    public DocumentoObservacionLectura(DocumentoObservacion original) {
        this.original = original;
    }

    public int getId() {
        return original.getId();
    }

    public DocumentoPostulacion getDocumento() {
        return original.getDocumento();
    }

    public ReferenciaId getEvaluador() {
        return original.getEvaluador() == null ? null : new ReferenciaId(original.getEvaluador().getId());
    }

    public TipoObservacion getTipoObservacion() {
        return original.getTipoObservacion();
    }

    public String getDescripcion() {
        return original.getDescripcion();
    }

    public LocalDate getFechaObservacion() {
        return original.getFechaObservacion();
    }

    public EstadoObservacion getEstadoObservacion() {
        return original.getEstadoObservacion();
    }

    public LocalDate getFechaSubsanacion() {
        return original.getFechaSubsanacion();
    }

    public String getComentarioSubsanacion() {
        return original.getComentarioSubsanacion();
    }
}
