package pe.edu.pucp.admitu.persona;

public class Evaluador extends Persona {
    private static int siguienteId = 1;
    private String cargo;
    private boolean activo;

    public Evaluador(String nombres, String apellidoPaterno, String apellidoMaterno, String correo
            , TipoDocumento tipoDocumento, String numeroDocumento, String telefono, String cargo) {
        super(nombres, apellidoPaterno, apellidoMaterno, correo, tipoDocumento, numeroDocumento, telefono);
        setId(siguienteId);
        siguienteId++;
        this.cargo = cargo;
        this.activo = true;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
