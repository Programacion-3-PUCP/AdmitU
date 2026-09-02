import java.time.LocalDate;

public class ConvocatoriaEtapa {
    private static int siguienteId = 1;
    private final int id;
    private Convocatoria convocatoria;
    private Etapa etapa;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public ConvocatoriaEtapa(Convocatoria convocatoria, Etapa etapa
            , LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = siguienteId++;
        this.convocatoria = convocatoria;
        this.etapa = etapa;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
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

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
