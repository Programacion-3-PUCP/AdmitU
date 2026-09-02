import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class ConvocatoriaModalidad {
    private static int siguienteId = 1;
    private final int id;
    private Convocatoria convocatoria;
    private Modalidad modalidad;
    private double costoInscripcion;
    private String observacion;
    private List<RequisitoConvocatoriaModalidad> requisitos;

    public ConvocatoriaModalidad(Convocatoria convocatoria, Modalidad modalidad, double costoInscripcion
            , String observacion, List<RequisitoConvocatoriaModalidad> requisitos) {
        this.id = siguienteId++;
        this.convocatoria = convocatoria;
        this.modalidad = modalidad;
        this.costoInscripcion = costoInscripcion;
        this.observacion = observacion;
        this.requisitos = new ArrayList<>(requisitos);
    }

    public int getId() {
        return id;
    }

    public Convocatoria getConvocatoria() {
        return convocatoria;
    }

    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public double getCostoInscripcion() {
        return costoInscripcion;
    }

    public void setCostoInscripcion(double costoInscripcion) {
        this.costoInscripcion = costoInscripcion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<RequisitoConvocatoriaModalidad> getRequisitos() {
        return new ArrayList<RequisitoConvocatoriaModalidad>(requisitos);
    }

    public void setRequisitos(List<RequisitoConvocatoriaModalidad> requisitos) {
        this.requisitos = requisitos;
    }
}