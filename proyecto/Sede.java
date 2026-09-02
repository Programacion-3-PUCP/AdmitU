public class Sede {
    private static int siguienteId = 1;
    private final int id;
    private String codigo;
    private String nombre;
    private String direccion;

    public Sede(String codigo, String nombre, String direccion) {
        this.id = siguienteId++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion; 
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

}
