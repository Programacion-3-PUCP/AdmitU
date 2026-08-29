class MedioPago {
    private static int correlativo = 1;
    private int idMedioPago;
    private String codigo;
    private String nombre;
    private String activo;

    public MedioPago(String codigo, String nombre) {
        this.idMedioPago = correlativo++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = "S";
    }

    public int getIdMedioPago() {
        return idMedioPago;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }
}