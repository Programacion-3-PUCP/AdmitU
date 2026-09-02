import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class PostulacionHistorial {
    private static int siguienteId = 1;
    private final int id;
    private Postulacion postulacion;
    private EstadoPostulacion estadoAnterior;
    private EstadoPostulacion estadoActual;
    private LocalDate fechaCambio;
    private String responsableCambio;
    private String motivoCambio;

    public PostulacionHistorial(Postulacion postulacion, EstadoPostulacion estadoAnterior
            , EstadoPostulacion estadoActual, LocalDate fechaCambio, String responsableCambio
            , String motivoCambio) {
        this.id = siguienteId++;
        this.postulacion = postulacion;
        this.estadoAnterior = estadoAnterior;
        this.estadoActual = estadoActual;
        this.fechaCambio = fechaCambio;
        this.responsableCambio = responsableCambio;
        this.motivoCambio = motivoCambio;
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

    public EstadoPostulacion getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoPostulacion estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoPostulacion getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(EstadoPostulacion estadoActual) {
        this.estadoActual = estadoActual;
    }

    public LocalDate getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDate fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getResponsableCambio() {
        return responsableCambio;
    }

    public void setResponsableCambio(String responsableCambio) {
        this.responsableCambio = responsableCambio;
    }

    public String getMotivoCambio() {
        return motivoCambio;
    }

    public void setMotivoCambio(String motivoCambio) {
        this.motivoCambio = motivoCambio;
    }
}