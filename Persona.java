abstract class Persona {
    private static int correlativo = 1;
    private int id;
    private String nombres;
    private String apellidos;
    private String dni;

    public Persona(String nombres, String apellidos, String dni) {
        this.id = correlativo++;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
    }

    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public abstract void mostrarInformacion();
}
