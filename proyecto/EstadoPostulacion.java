public class EstadoPostulacion {
    private static int siguienteId = 1;
    private final int id;
    private String codigo;
    private String nombre;
    private String descripcion;

    public EstadoPostulacion(String codigo, String nombre, String descripcion) {
        this.id = siguienteId++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
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
}
