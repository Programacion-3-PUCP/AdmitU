import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Convocatoria {
    private static int correlativo = 1;
    private int idConvocatoria;
    private String periodo;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado;
    private List<ConvocaModalidad> modalidades;
    private List<OfertaCarrera> ofertas;
    private List<ReqConvModalidad> requisitos;
    private List<ConvocaEtapa> etapas;

    public Convocatoria(String periodo, Date fechaInicio, Date fechaFin) {
        this.idConvocatoria = correlativo++;
        this.periodo = periodo;
        this.fechaInicio = (Date) fechaInicio.clone();
        this.fechaFin = (Date) fechaFin.clone();
        this.estado = "B";
        this.modalidades = new ArrayList<>();
        this.ofertas = new ArrayList<>();
        this.requisitos = new ArrayList<>();
        this.etapas = new ArrayList<>();
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<ConvocaModalidad> getModalidades() {
        return new ArrayList<>(modalidades);
    }

    public void setModalidades(List<ConvocaModalidad> modalidades) {
        this.modalidades = new ArrayList<>(modalidades);
    }

    public List<OfertaCarrera> getOfertas() {
        return new ArrayList<>(ofertas);
    }

    public void setOfertas(List<OfertaCarrera> ofertas) {
        this.ofertas = new ArrayList<>(ofertas);
    }

    public List<ReqConvModalidad> getRequisitos() {
        return new ArrayList<>(requisitos);
    }

    public void setRequisitos(List<ReqConvModalidad> requisitos) {
        this.requisitos = new ArrayList<>(requisitos);
    }

    public List<ConvocaEtapa> getEtapas() {
        return new ArrayList<>(etapas);
    }

    public void setEtapas(List<ConvocaEtapa> etapas) {
        this.etapas = new ArrayList<>(etapas);
    }
}