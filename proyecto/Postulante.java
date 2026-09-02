import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Postulante extends Persona {
    private static int siguienteId = 1;
    private Apoderado apoderado;
    private LocalDate fechaNacimiento;
    private boolean tieneDiscapacidad;
    private String numeroCarnetConadis;
    private boolean correoValidado;
    private LocalDate fechaValidacionCorreo;
    private List<AntecedenteAcademico> antecedentes ;
    private List<Postulacion> postulaciones ;

    public Postulante(String nombres, String apellidoPaterno, String apellidoMaterno, String correo
            , TipoDocumento tipoDocumento, String numeroDocumento, String telefono, Apoderado apoderado
            , LocalDate fechaNacimiento, boolean tieneDiscapacidad, String numeroCarnetConadis
            , boolean correoValidado, LocalDate fechaValidacionCorreo, List<AntecedenteAcademico> antecedentes
            , List<Postulacion> postulaciones) {
        super(nombres, apellidoPaterno, apellidoMaterno, correo, tipoDocumento, numeroDocumento, telefono);
        setId(siguienteId);
        siguienteId++;
        this.apoderado = apoderado;
        this.fechaNacimiento = fechaNacimiento;
        this.tieneDiscapacidad = tieneDiscapacidad;
        this.numeroCarnetConadis = numeroCarnetConadis;
        this.correoValidado = correoValidado;
        this.fechaValidacionCorreo = fechaValidacionCorreo;
        this.antecedentes = new ArrayList<>(antecedentes);
        this.postulaciones = new ArrayList<>(postulaciones);
    }

    public Apoderado getApoderado() {
        return apoderado;
    }

    public void setApoderado(Apoderado apoderado) {
        this.apoderado = apoderado;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public boolean isTieneDiscapacidad() {
        return tieneDiscapacidad;
    }

    public void setTieneDiscapacidad(boolean tieneDiscapacidad) {
        this.tieneDiscapacidad = tieneDiscapacidad;
    }

    public String getNumeroCarnetConadis() {
        return numeroCarnetConadis;
    }

    public void setNumeroCarnetConadis(String numeroCarnetConadis) {
        this.numeroCarnetConadis = numeroCarnetConadis;
    }

    public boolean isCorreoValidado() {
        return correoValidado;
    }

    public void setCorreoValidado(boolean correoValidado) {
        this.correoValidado = correoValidado;
    }

    public LocalDate getFechaValidacionCorreo() {
        return fechaValidacionCorreo;
    }

    public void setFechaValidacionCorreo(LocalDate fechaValidacionCorreo) {
        this.fechaValidacionCorreo = fechaValidacionCorreo;
    }

    public List<AntecedenteAcademico> getAntecedentes() {
        return new ArrayList<AntecedenteAcademico>(antecedentes);
    }

    public void setAntecedentes(List<AntecedenteAcademico> antecedentes) {
        this.antecedentes = antecedentes;
    }

    public List<Postulacion> getPostulaciones() {
        return new ArrayList<Postulacion>(postulaciones);
    }

    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones = postulaciones;
    }
}
