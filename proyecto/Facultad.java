public class Facultad {
    private static int siguienteId = 1;
    private final int id;
    private String codigo;
    private String nombre;

    public Facultad(String codigo, String nombre) {
        this.id = siguienteId++;
        this.codigo = codigo;
        this.nombre = nombre;
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
}