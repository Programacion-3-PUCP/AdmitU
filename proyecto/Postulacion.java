import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Postulacion {
    private static int siguienteId = 1;
    private final int id;
    private Postulante postulante;
    private Convocatoria convocatoria;
    private ConvocatoriaModalidad modalidadElegida;
    private OfertaCarrera carreraElegida;
    private EstadoPostulacion estadoActual;
    private LocalDate fechaRegistro;
    private LocalDate fechaEnvio;
    private LocalDate fechaFinalizacion;
    private String codigoInscripcion;
    private String observacionGeneral;
    private List<PostulacionHistorial> historial ;
    private List<Pago> pagos ;
    private List<DocumentoPostulacion> documentos ;
    private List<Notificacion> notificaciones ;
    private CarnePostulante carne;

    public Postulacion(){
        this.id = siguienteId++;
    }

    public Postulacion(Postulante postulante, Convocatoria convocatoria, ConvocatoriaModalidad modalidadElegida
            , OfertaCarrera carreraElegida, EstadoPostulacion estadoActual, LocalDate fechaRegistro
            , LocalDate fechaEnvio, LocalDate fechaFinalizacion, String codigoInscripcion
            , String observacionGeneral, List<PostulacionHistorial> historial, List<Pago> pagos
            , List<DocumentoPostulacion> documentos, List<Notificacion> notificaciones, CarnePostulante carne) {
        this.id = siguienteId++;
        this.postulante = postulante;
        this.convocatoria = convocatoria;
        this.modalidadElegida = modalidadElegida;
        this.carreraElegida = carreraElegida;
        this.estadoActual = estadoActual;
        this.fechaRegistro = fechaRegistro;
        this.fechaEnvio = fechaEnvio;
        this.fechaFinalizacion = fechaFinalizacion;
        this.codigoInscripcion = codigoInscripcion;
        this.observacionGeneral = observacionGeneral;
        this.historial = new ArrayList<>(historial);
        this.pagos = new ArrayList<>(pagos);
        this.documentos = new ArrayList<>(documentos);
        this.notificaciones = new ArrayList<>(notificaciones);
        this.carne = carne;
    }

    public int getId() {
        return id;
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

    public ConvocatoriaModalidad getModalidadElegida() {
        return modalidadElegida;
    }

    public void setModalidadElegida(ConvocatoriaModalidad modalidadElegida) {
        this.modalidadElegida = modalidadElegida;
    }

    public OfertaCarrera getCarreraElegida() {
        return carreraElegida;
    }

    public void setCarreraElegida(OfertaCarrera carreraElegida) {
        this.carreraElegida = carreraElegida;
    }

    public EstadoPostulacion getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(EstadoPostulacion estadoActual) {
        this.estadoActual = estadoActual;
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

    public String getCodigoInscripcion() {
        return codigoInscripcion;
    }

    public void setCodigoInscripcion(String codigoInscripcion) {
        this.codigoInscripcion = codigoInscripcion;
    }

    public String getObservacionGeneral() {
        return observacionGeneral;
    }

    public void setObservacionGeneral(String observacionGeneral) {
        this.observacionGeneral = observacionGeneral;
    }

    public List<PostulacionHistorial> getHistorial() {
        return new ArrayList<PostulacionHistorial>(historial);
    }

    public void setHistorial(List<PostulacionHistorial> historial) {
        this.historial = historial;
    }

    public List<Pago> getPagos() {
        return  new ArrayList<Pago>(pagos);
    }

    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }

    public List<DocumentoPostulacion> getDocumentos() {
        return new ArrayList<DocumentoPostulacion>(documentos);
    }

    public void setDocumentos(List<DocumentoPostulacion> documentos) {
        this.documentos = documentos;
    }

    public List<Notificacion> getNotificaciones() {
        return new ArrayList<Notificacion>(notificaciones);
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public CarnePostulante getCarne() {
        return carne;
    }

    public void setCarne(CarnePostulante carne) {
        this.carne = carne;
    }
}