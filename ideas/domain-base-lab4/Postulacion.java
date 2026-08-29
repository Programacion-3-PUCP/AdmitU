import java.util.Date;

class Postulacion {
    private static int correlativo = 1;
    private int idPostulacion;
    private Postulante postulante;
    private Convocatoria convocatoria;
    private ConvocaModalidad convocatoriaModalidad;
    private OfertaCarrera ofertaCarrera;
    private EstadoPostulacion estadoActual;
    private Date fechaCreacion;
    private Date fechaFinalizacion;

    public Postulacion(Postulante postulante, Convocatoria convocatoria,
                       ConvocaModalidad convocatoriaModalidad, OfertaCarrera ofertaCarrera,
                       EstadoPostulacion estadoInicial) {
        this.idPostulacion = ++correlativo;
        this.postulante = postulante;
        this.convocatoria = convocatoria;
        this.convocatoriaModalidad = convocatoriaModalidad;
        this.ofertaCarrera = ofertaCarrera;
        this.estadoActual = estadoInicial;
        this.fechaCreacion = new Date();
        this.fechaFinalizacion = null;
    }

    public int getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(int idPostulacion) {
        this.idPostulacion = idPostulacion;
    }

    public Postulante getPostulante() {
        return postulante;
    }

    public void setPostulante(Postulante postulante) {
        this.postulante = postulante;
    }

    public Convocatoria getConvocatoria() {
        return convocatoria;
    }

    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
    }

    public ConvocaModalidad getConvocatoriaModalidad() {
        return convocatoriaModalidad;
    }

    public void setConvocatoriaModalidad(ConvocaModalidad convocatoriaModalidad) {
        this.convocatoriaModalidad = convocatoriaModalidad;
    }

    public OfertaCarrera getOfertaCarrera() {
        return ofertaCarrera;
    }

    public void setOfertaCarrera(OfertaCarrera ofertaCarrera) {
        this.ofertaCarrera = ofertaCarrera;
    }

    public EstadoPostulacion getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(EstadoPostulacion estadoActual) {
        this.estadoActual = estadoActual;
    }

    public Date getFechaCreacion() {
        return (Date) fechaCreacion.clone();
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = (Date) fechaCreacion.clone();
    }

    public Date getFechaFinalizacion() {
        return (Date) fechaFinalizacion.clone();
    }

    public void setFechaFinalizacion(Date fechaFinalizacion) {
        this.fechaFinalizacion = (Date) fechaFinalizacion.clone();
    }
}