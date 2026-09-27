package pe.edu.pucp.admitu.persona;

public class Pais {
    private static int siguienteId = 1;
    private final int id;
    private String codigoIso2;
    private String nombre;
    private boolean activo;

    public Pais(String codigoIso2, String nombre) {
        this.id = siguienteId++;
        this.codigoIso2 = codigoIso2;
        this.nombre = nombre;
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    public String getCodigoIso2() {
        return codigoIso2;
    }

    public void setCodigoIso2(String codigoIso2) {
        this.codigoIso2 = codigoIso2;
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
