package pe.edu.pucp.admitu.postulacion;

import java.time.LocalDate;
import java.util.List;

/** Contrato de lectura global: no hereda entidades personales ni texto libre. */
public final class ExpedienteResumen {
    private final int id;
    private final String codigoInscripcion;
    private final int convocatoriaId, modalidadElegidaId, carreraElegidaId, estadoActualId;
    private final Integer evaluadorId;
    private final LocalDate fechaRegistro, fechaEnvio, fechaFinalizacion;
    private final List<ObservacionResumen> observaciones;

    public ExpedienteResumen(Postulacion p, List<ObservacionResumen> observaciones) {
        id = p.getId(); codigoInscripcion = p.getCodigoInscripcion();
        convocatoriaId = p.getConvocatoria().getId(); modalidadElegidaId = p.getModalidadElegida().getId();
        carreraElegidaId = p.getCarreraElegida().getId(); estadoActualId = p.getEstadoActual().getId();
        evaluadorId = p.getEvaluadorId(); fechaRegistro = p.getFechaRegistro();
        fechaEnvio = p.getFechaEnvio(); fechaFinalizacion = p.getFechaFinalizacion();
        this.observaciones = List.copyOf(observaciones);
    }
    public int getId() { return id; }
    public String getCodigoInscripcion() { return codigoInscripcion; }
    public int getConvocatoriaId() { return convocatoriaId; }
    public int getModalidadElegidaId() { return modalidadElegidaId; }
    public int getCarreraElegidaId() { return carreraElegidaId; }
    public int getEstadoActualId() { return estadoActualId; }
    public Integer getEvaluadorId() { return evaluadorId; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public LocalDate getFechaEnvio() { return fechaEnvio; }
    public LocalDate getFechaFinalizacion() { return fechaFinalizacion; }
    public List<ObservacionResumen> getObservaciones() { return observaciones; }
}
