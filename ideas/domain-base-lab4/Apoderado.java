class Apoderado extends Persona {
    private String celular;
    private String correo;
    private int parentescoId;

    public Apoderado(String nombres, String apellidos, String dni) {
        super(nombres, apellidos, dni);
        this.celular = "";
        this.correo = "";
        this.parentescoId = 0;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getParentescoId() {
        return parentescoId;
    }

    public void setParentescoId(int parentescoId) {
        this.parentescoId = parentescoId;
    }
}