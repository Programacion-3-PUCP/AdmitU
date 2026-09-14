package pe.edu.pucp.admitu.configuracion;
import pe.edu.pucp.admitu.persona.Postulante;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Convocatoria {
    private static int siguienteId = 1;
    private final int id;
    private String codigoConvocatoria;
    private String nombre;
    private String periodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoConvocatoria estado;
    private String descripcion;

    private boolean activo;
    private List<Postulante> postulantes;
    private List<ConvocatoriaModalidad> modalidadesHabilitadas ;
    private List<OfertaCarrera> carrerasOfrecidas ;
    private List<ConvocatoriaEtapa> etapas ;

    public Convocatoria(){
        this.id = siguienteId++;
        this.activo = true;
        this.postulantes = new ArrayList<>();
        this.modalidadesHabilitadas = new ArrayList<>();
        this.carrerasOfrecidas = new ArrayList<>();
        this.etapas = new ArrayList<>();
    }

    public Convocatoria(String codigoConvocatoria, String nombre, String periodo
            , LocalDate fechaInicio, LocalDate fechaFin, EstadoConvocatoria estado
            , String descripcion, List<Postulante> postulantes, List<ConvocatoriaModalidad> modalidadesHabilitadas
            , List<OfertaCarrera> carrerasOfrecidas, List<ConvocatoriaEtapa> etapas) {
        this.id = siguienteId++;
        this.codigoConvocatoria = codigoConvocatoria;
        this.nombre = nombre;
        this.periodo = periodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.descripcion = descripcion;
        this.activo = true;
        this.postulantes = (postulantes == null) ? new ArrayList<>() : new ArrayList<>(postulantes);
        this.modalidadesHabilitadas = (modalidadesHabilitadas == null) ? new ArrayList<>() : new ArrayList<>(modalidadesHabilitadas);
        this.carrerasOfrecidas = (carrerasOfrecidas == null) ? new ArrayList<>() : new ArrayList<>(carrerasOfrecidas);
        this.etapas = (etapas == null) ? new ArrayList<>() : new ArrayList<>(etapas);
    }

    public int getId() {
        return id;
    }

    public String getCodigoConvocatoria() {
        return codigoConvocatoria;
    }

    public void setCodigoConvocatoria(String codigoConvocatoria) {
        this.codigoConvocatoria = codigoConvocatoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
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

    public EstadoConvocatoria getEstado() {
        return estado;
    }

    public void setEstado(EstadoConvocatoria estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Postulante> getPostulantes() {
        return new ArrayList<Postulante>(postulantes);
    }

    public void setPostulantes(List<Postulante> postulantes) {
        this.postulantes = (postulantes == null) ? new ArrayList<>() : new ArrayList<>(postulantes);
    }

    public List<OfertaCarrera> getCarrerasOfrecidas() {
        return new ArrayList<OfertaCarrera>(carrerasOfrecidas);
    }

    public void setCarrerasOfrecidas(List<OfertaCarrera> carrerasOfrecidas) {
        this.carrerasOfrecidas = (carrerasOfrecidas == null) ? new ArrayList<>() : new ArrayList<>(carrerasOfrecidas);
    }

    public List<ConvocatoriaModalidad> getModalidadesHabilitadas() {
        return new ArrayList<ConvocatoriaModalidad>(modalidadesHabilitadas);
    }

    public void setModalidadesHabilitadas(List<ConvocatoriaModalidad> modalidadesHabilitadas) {
        this.modalidadesHabilitadas = (modalidadesHabilitadas == null) ? new ArrayList<>() : new ArrayList<>(modalidadesHabilitadas);
    }

    public List<ConvocatoriaEtapa> getEtapas() {
        return new ArrayList<ConvocatoriaEtapa>(etapas);
    }

    public void setEtapas(List<ConvocatoriaEtapa> etapas) {
        this.etapas = (etapas == null) ? new ArrayList<>() : new ArrayList<>(etapas);
    }
}
