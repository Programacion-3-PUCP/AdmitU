class EstadoPostulacion {
    private static int correlativo = 1;
    private int idEstado;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String activo;

    public EstadoPostulacion(String codigo, String nombre, String descripcion) {
        this.idEstado = correlativo++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = "S";
    }

    public int getIdEstado() {
        return idEstado;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }
}