public class Carrera {
    private static int siguienteId = 1;
    private final int id;
    private Facultad facultad;
    private String codigoCarrera;
    private String nombre;

    public Carrera(Facultad facultad, String codigoCarrera, String nombre) {
        this.id = siguienteId++;
        this.facultad = facultad;
        this.codigoCarrera = codigoCarrera;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    public String getCodigoCarrera() {
        return codigoCarrera;
    }

    public void setCodigoCarrera(String codigoCarrera) {
        this.codigoCarrera = codigoCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}