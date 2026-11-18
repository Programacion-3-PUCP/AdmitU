package pe.edu.pucp.admitu.postulacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteResumen {
    private int id;
    private String codigoInscripcion;
    private int convocatoriaId;
    private int modalidadElegidaId;
    private int carreraElegidaId;
    private int estadoActualId;
    private Integer evaluadorId;
    private LocalDate fechaRegistro;
    private LocalDate fechaEnvio;
    private LocalDate fechaFinalizacion;
    private List<ObservacionResumen> observaciones;

    public ExpedienteResumen() {
        this.observaciones = new ArrayList<>();
    }

    public ExpedienteResumen(Postulacion p, List<ObservacionResumen> observaciones) {
        this.id = p.getId();
        this.codigoInscripcion = p.getCodigoInscripcion();
        this.convocatoriaId = p.getConvocatoria() != null ? p.getConvocatoria().getId() : 0;
        this.modalidadElegidaId = p.getModalidadElegida() != null ? p.getModalidadElegida().getId() : 0;
        this.carreraElegidaId = p.getCarreraElegida() != null ? p.getCarreraElegida().getId() : 0;
        this.estadoActualId = p.getEstadoActual() != null ? p.getEstadoActual().getId() : 0;
        this.evaluadorId = p.getEvaluadorId();
        this.fechaRegistro = p.getFechaRegistro();
        this.fechaEnvio = p.getFechaEnvio();
        this.fechaFinalizacion = p.getFechaFinalizacion();
        this.observaciones = observaciones != null ? new ArrayList<>(observaciones) : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoInscripcion() {
        return codigoInscripcion;
    }

    public void setCodigoInscripcion(String codigoInscripcion) {
        this.codigoInscripcion = codigoInscripcion;
    }

    public int getConvocatoriaId() {
        return convocatoriaId;
    }

    public void setConvocatoriaId(int convocatoriaId) {
        this.convocatoriaId = convocatoriaId;
    }

    public int getModalidadElegidaId() {
        return modalidadElegidaId;
    }

    public void setModalidadElegidaId(int modalidadElegidaId) {
        this.modalidadElegidaId = modalidadElegidaId;
    }

    public int getCarreraElegidaId() {
        return carreraElegidaId;
    }

    public void setCarreraElegidaId(int carreraElegidaId) {
        this.carreraElegidaId = carreraElegidaId;
    }

    public int getEstadoActualId() {
        return estadoActualId;
    }

    public void setEstadoActualId(int estadoActualId) {
        this.estadoActualId = estadoActualId;
    }

    public Integer getEvaluadorId() {
        return evaluadorId;
    }

    public void setEvaluadorId(Integer evaluadorId) {
        this.evaluadorId = evaluadorId;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDate fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDate getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDate fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public List<ObservacionResumen> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<ObservacionResumen> observaciones) {
        this.observaciones = observaciones;
    }
}
