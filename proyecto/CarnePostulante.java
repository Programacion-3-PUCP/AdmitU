import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class CarnePostulante {
    private static int siguienteId = 1;
    private final int id;
    private Postulacion postulacion;
    private Sede sede;
    private String codigoCarne;
    private LocalDate fechaGeneracion;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private String aulaExamen;

    public CarnePostulante(Postulacion postulacion, Sede sede, String codigoCarne
            , LocalDate fechaGeneracion, LocalDate fechaInicioVigencia
            , LocalDate fechaFinVigencia, String aulaExamen) {
        this.id = siguienteId++;
        this.postulacion = postulacion;
        this.sede = sede;
        this.codigoCarne = codigoCarne;
        this.fechaGeneracion = fechaGeneracion;
        this.fechaInicioVigencia = fechaInicioVigencia;
        this.fechaFinVigencia = fechaFinVigencia;
        this.aulaExamen = aulaExamen;
    }

    public int getId() {
        return id;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public String getCodigoCarne() {
        return codigoCarne;
    }

    public void setCodigoCarne(String codigoCarne) {
        this.codigoCarne = codigoCarne;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDate getFechaInicioVigencia() {
        return fechaInicioVigencia;
    }

    public void setFechaInicioVigencia(LocalDate fechaInicioVigencia) {
        this.fechaInicioVigencia = fechaInicioVigencia;
    }

    public LocalDate getFechaFinVigencia() {
        return fechaFinVigencia;
    }

    public void setFechaFinVigencia(LocalDate fechaFinVigencia) {
        this.fechaFinVigencia = fechaFinVigencia;
    }

    public String getAulaExamen() {
        return aulaExamen;
    }

    public void setAulaExamen(String aulaExamen) {
        this.aulaExamen = aulaExamen;
    }
}