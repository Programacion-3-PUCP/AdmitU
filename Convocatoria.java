import java.util.Date;
import java.util.List;
import java.util.ArrayList;

class Convocatoria {
    private static int correlativo = 1;
    private int idConvocatoria;
    private String periodo;
    private Date fechaInicio;
    private Date fechaFin;
    private List<Postulante> postulantes;

    public Convocatoria(String periodo, Date fechaInicio, Date fechaFin) {
        this.idConvocatoria = correlativo++;
        this.periodo = periodo;
        this.fechaInicio = (Date) fechaInicio.clone();
        this.fechaFin = (Date) fechaFin.clone();
        this.postulantes = new ArrayList<>();
    }

    public int getIdConvocatoria() {
        return idConvocatoria;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Date getFechaInicio() {
        return (Date) fechaInicio.clone();
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = (Date) fechaInicio.clone();
    }

    public Date getFechaFin() {
        return (Date) fechaFin.clone();
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = (Date) fechaFin.clone();
    }

    public List<Postulante> getPostulantes() {
        return new ArrayList<>(postulantes);
    }

    public void agregarPostulante(Postulante postulante) {
        this.postulantes.add(postulante);
    }
}
