import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Notificacion {
    private static int siguienteId = 1;
    private final int id;
    private Postulacion postulacion;
    private DocumentoObservacion observacionOrigen;
    private MedioNotificacion medioNotificacion;
    private TipoNotificacion tipoNotificacion;
    private String destinatario;
    private String asunto;
    private String mensaje;
    private LocalDate fechaProgramada;
    private LocalDate fechaEnvio;
    private EstadoEnvio estadoEnvio;

    public Notificacion(Postulacion postulacion, DocumentoObservacion observacionOrigen
            , MedioNotificacion medioNotificacion, TipoNotificacion tipoNotificacion
            , String destinatario, String asunto, String mensaje, LocalDate fechaProgramada
            , LocalDate fechaEnvio, EstadoEnvio estadoEnvio) {
        this.id = siguienteId++;
        this.postulacion = postulacion;
        this.observacionOrigen = observacionOrigen;
        this.medioNotificacion = medioNotificacion;
        this.tipoNotificacion = tipoNotificacion;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaProgramada = fechaProgramada;
        this.fechaEnvio = fechaEnvio;
        this.estadoEnvio = estadoEnvio;
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

    public DocumentoObservacion getObservacionOrigen() {
        return observacionOrigen;
    }

    public void setObservacionOrigen(DocumentoObservacion observacionOrigen) {
        this.observacionOrigen = observacionOrigen;
    }

    public MedioNotificacion getMedioNotificacion() {
        return medioNotificacion;
    }

    public void setMedioNotificacion(MedioNotificacion medioNotificacion) {
        this.medioNotificacion = medioNotificacion;
    }

    public TipoNotificacion getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(TipoNotificacion tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDate fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(EstadoEnvio estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }
}