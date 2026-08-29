import java.util.Date;

class Postulante extends Persona {
    private String correo;
    private String correoValidado;
    private Date fechaValidacionCorreo;
    private int apoderadoId;
    private int institucionEducativaId;

    public Postulante(String nombres, String apellidos, String dni, String correo) {
        super(nombres, apellidos, dni);
        this.correo = correo;
        this.correoValidado = "N";
        this.fechaValidacionCorreo = null;
        this.apoderadoId = 0;
        this.institucionEducativaId = 0;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCorreoValidado() {
        return correoValidado;
    }

    public void setCorreoValidado(String correoValidado) {
        this.correoValidado = correoValidado;
    }

    public Date getFechaValidacionCorreo() {
        return (Date) fechaValidacionCorreo.clone();
    }

    public void setFechaValidacionCorreo(Date fechaValidacionCorreo) {
        this.fechaValidacionCorreo = (Date) fechaValidacionCorreo.clone();
    }

    public int getApoderadoId() {
        return apoderadoId;
    }

    public void setApoderadoId(int apoderadoId) {
        this.apoderadoId = apoderadoId;
    }

    public int getInstitucionEducativaId() {
        return institucionEducativaId;
    }

    public void setInstitucionEducativaId(int institucionEducativaId) {
        this.institucionEducativaId = institucionEducativaId;
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("Postulante: " + getNombres() + " " + getApellidos() + " - DNI: " + getDni() + " - Correo: " + correo + " - Validado: " + correoValidado);
    }
}