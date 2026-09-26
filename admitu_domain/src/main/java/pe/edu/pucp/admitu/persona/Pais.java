package pe.edu.pucp.admitu.persona;

public class Pais {
    private int id;
    private String codigoIso2;
    private String nombre;

    public Pais(String codigoIso2, String nombre) {
        this.codigoIso2 = codigoIso2;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {this.id = id;}

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
}
