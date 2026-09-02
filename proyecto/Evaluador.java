public class Evaluador extends Persona {
    private static int siguienteId = 1;
    private String cargo;

    public Evaluador(String nombres, String apellidoPaterno, String apellidoMaterno, String correo
            , TipoDocumento tipoDocumento, String numeroDocumento, String telefono, String cargo) {
        super(nombres, apellidoPaterno, apellidoMaterno, correo, tipoDocumento, numeroDocumento, telefono);
        setId(siguienteId);
        siguienteId++;
        this.cargo = cargo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

}
