package pe.edu.pucp.admitu.configuracion;

public class Facultad {
    private static int siguienteId = 1;
    private int id;
    private String codigo;
    private String nombre;
    private boolean activo;

    public Facultad(String codigo, String nombre) {
        this.id = siguienteId++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        if (id >= siguienteId) siguienteId = id + 1;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}